package pl.ib.beauty.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pl.ib.beauty.model.CourseType;
import pl.ib.beauty.model.dao.Address;
import pl.ib.beauty.model.dao.Category;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.RepublishCourseRequest;
import pl.ib.beauty.repository.CategoryRepository;
import pl.ib.beauty.repository.CourseRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final GeoCodingService geoCodingService;

    @Transactional(readOnly = true)
    public Page<Course> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @Transactional
    public List<User> getCourseParticipants(Long courseId) {
        Optional<Course> courseOptional = courseRepository.findById(courseId);
        return courseOptional.map(Course::getParticipants)
                .orElseGet(ArrayList::new);
    }

    @Transactional
    public Page<Course> getAllCurrentCreatorCourses(Pageable pageable) {
        User currentLoginUser = getUser();
        return courseRepository.findByCreatorId(currentLoginUser.getId(), pageable);
    }

    private User getUser() {
        User currentLoginUser = userService.currentLoginUser();
        return currentLoginUser;
    }

    @Transactional(readOnly = true)
    public Page<Course> getCoursesByCategoryAndTitle(Long categoryId, String title, String city, CourseType courseType,
                                                     Pageable pageable, boolean isCurrentCreator,
                                                     boolean isCurrentStudent, boolean hidePast) {
        if (isCurrentCreator) {
            Long currentUserId = userService.currentLoginUser().getId();
            return courseRepository.findByCreatorId(currentUserId, pageable);
        }
        if (isCurrentStudent) {
            Long currentUserId = userService.currentLoginUser().getId();
            return courseRepository.findByParticipantsId(currentUserId, pageable);
        }
        return courseRepository.findAll(buildPublicCourseSpec(categoryId, title, city, courseType, hidePast), pageable);
    }

    private Specification<Course> buildPublicCourseSpec(Long categoryId, String title, String city,
                                                        CourseType courseType, boolean hidePast) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (title != null && !title.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            if (city != null && !city.isBlank()) {
                var addr = root.join("address", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(addr.get("city")), "%" + city.toLowerCase() + "%"));
            }
            if (courseType != null) {
                predicates.add(cb.equal(root.get("courseType"), courseType));
            }
            if (hidePast) {
                LocalDateTime now = LocalDateTime.now();
                predicates.add(cb.or(
                    cb.isNull(root.get("endDate")),
                    cb.greaterThanOrEqualTo(root.get("endDate"), now)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public List<String> getDistinctCities() {
        return courseRepository.findDistinctCities();
    }

    @Transactional
    @CachePut(cacheNames = "course", key = "#result.id")
    public Course createCourse(Course course) {
        String label = course.getCategory().getLabel();
        Category category = categoryRepository.findByLabel(label)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with label: " + label));
        course.setCategory(category);
        course.setCreator(userService.currentLoginUser());
        if (course.getCourseType() == null) {
            course.setCourseType(CourseType.IN_PERSON);
        }
        if (course.getAddress() != null) {
            geoCodingService.locate(course.getAddress());
        }
        return courseRepository.save(course);
    }

    // Every write refreshes the cached copy that getCourseById serves, so edits, deletes and new
    // participants show up immediately instead of after the cache TTL.
    @Transactional
    @CachePut(cacheNames = "course", key = "#id")
    public Course updateCourse(Long id, Course course) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + id));

        // People who already booked keep their seats: the group can't shrink below them.
        int booked = existingCourse.getParticipants() != null ? existingCourse.getParticipants().size() : 0;
        if (course.getMaxParticipants() < booked) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The group can't be smaller than the " + booked + " people already enrolled");
        }

        existingCourse.setTitle(course.getTitle());
        existingCourse.setDescription(course.getDescription());
        existingCourse.setStartDate(course.getStartDate());
        existingCourse.setEndDate(course.getEndDate());
        existingCourse.setMaxParticipants(course.getMaxParticipants());
        existingCourse.setCourseType(course.getCourseType() != null ? course.getCourseType() : CourseType.IN_PERSON);
        // A new price applies to new bookings; what people paid is in their payments.
        existingCourse.setPrice(course.getPrice());
        if (course.getCourseLevel() != null) existingCourse.setCourseLevel(course.getCourseLevel());
        if (course.getStatus() != null) existingCourse.setStatus(course.getStatus());
        if (course.getLanguage() != null) existingCourse.setLanguage(course.getLanguage());
        existingCourse.setPrerequisites(course.getPrerequisites());
        existingCourse.setLearningOutcomes(course.getLearningOutcomes());
        existingCourse.setCertificate(course.isCertificate());
        locateChangedAddress(course.getAddress(), existingCourse.getAddress());
        existingCourse.setAddress(course.getAddress());
        existingCourse.setCategory(resolveCategory(course.getCategory()));

        return courseRepository.save(existingCourse);
    }

    /** The category by id when given, else by label: the course wizard knows categories by their label. */
    private Category resolveCategory(Category requested) {
        if (requested == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A course needs a category");
        }
        if (requested.getId() != null) {
            return categoryRepository.findById(requested.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + requested.getId()));
        }
        return categoryRepository.findByLabel(requested.getLabel())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with label: " + requested.getLabel()));
    }

    /** An edited address keeps its map pin when the place is the same, and is looked up again when it moved. */
    private void locateChangedAddress(Address address, Address previous) {
        if (address == null) {
            return;
        }
        boolean samePlace = previous != null && previous.getLat() != null
                && GeoCodingService.queryFor(previous).equals(GeoCodingService.queryFor(address));
        if (samePlace) {
            address.setLat(previous.getLat());
            address.setLng(previous.getLng());
        } else {
            geoCodingService.locate(address);
        }
    }

    @Transactional
    @CachePut(cacheNames = "course", key = "#id")
    public Course updateCover(Long id, String imageUrl) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));
        course.setImageUrl(imageUrl);
        return courseRepository.save(course);
    }

    @Transactional
    @CachePut(cacheNames = "course", key = "#id")
    public Course updateRating(Long id, double average, int count) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));
        course.setRating(average);
        course.setRatingCount(count);
        return courseRepository.save(course);
    }

    @Transactional
    @CacheEvict(cacheNames = "course", key = "#id")
    public void deleteCourseById(Long id) {
        courseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "course", key = "#id")
    public Course getCourseById(Long id) {
        log.info("Get course with id: {}", id);
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));
    }

    public List<String> getAutocompleteTitle(String title) {
        List<Course> courses = courseRepository.findByTitleContainingIgnoreCase(title);
        return courses.stream()
                .map(Course::getTitle)
                .distinct()
                .toList();
    }

    public List<Course> findByDate(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        User currentLoginUser = userService.currentLoginUser();

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);
        return courseRepository.findCoursesOverlapping(currentLoginUser.getId(), fromDateTime, toDateTime);
    }

    @Transactional
    @CachePut(cacheNames = "course", key = "#result.id")
    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    @Transactional
    public Course republishCourse(Long originalId, RepublishCourseRequest request) {
        Course original = courseRepository.findById(originalId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + originalId));

        Address newAddress = null;
        if (original.getAddress() != null) {
            Address src = original.getAddress();
            newAddress = Address.builder()
                    .district(src.getDistrict())
                    .street(src.getStreet())
                    .streetNumber(src.getStreetNumber())
                    .apartmentNumber(src.getApartmentNumber())
                    .city(src.getCity())
                    .postalCode(src.getPostalCode())
                    .lat(src.getLat())
                    .lng(src.getLng())
                    .build();
        }

        Course republished = Course.builder()
                .title(original.getTitle())
                .description(original.getDescription())
                .price(original.getPrice())
                .maxParticipants(original.getMaxParticipants())
                .courseType(original.getCourseType())
                .category(original.getCategory())
                .creator(original.getCreator())
                .address(newAddress)
                .startDate(request.newStartDate())
                .endDate(request.newEndDate())
                .parentCourse(original)
                .build();

        return courseRepository.save(republished);
    }
}

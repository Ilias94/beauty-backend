package pl.ib.beauty.service;

import com.google.maps.model.LatLng;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
            String address = course.getAddress().getStreet() + ", " + course.getAddress().getStreetNumber() + ", " +
                    course.getAddress().getApartmentNumber() + ", " + course.getAddress().getCity() + ", " + course.getAddress().getPostalCode();
            LatLng latLng = geoCodingService.getGeoCoding(address);
            if (null != latLng) {
                course.getAddress().setLat(latLng.lat);
                course.getAddress().setLng(latLng.lng);
            }
        }
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long id, Course course) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + id));

        existingCourse.setTitle(course.getTitle());
        existingCourse.setDescription(course.getDescription());
        existingCourse.setStartDate(course.getStartDate());
        existingCourse.setEndDate(course.getEndDate());
        existingCourse.setMaxParticipants(course.getMaxParticipants());
        existingCourse.setCourseType(course.getCourseType() != null ? course.getCourseType() : CourseType.IN_PERSON);
        existingCourse.setAddress(course.getAddress());

        Long categoryId = course.getCategory().getId();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        existingCourse.setCategory(category);

        return courseRepository.save(existingCourse);
    }

    @Transactional
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

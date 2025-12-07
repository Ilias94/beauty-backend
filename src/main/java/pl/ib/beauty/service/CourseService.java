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
import pl.ib.beauty.model.dao.Category;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.User;
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
        User currentLoginUser = userService.currentLoginUser();
        return courseRepository.findByCreatorId(currentLoginUser.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Course> getCoursesByCategoryAndTitle(Long categoryId, String title, Pageable pageable, boolean isCurrentCreator, boolean isCurrentStudent) {
        if (isCurrentCreator) {
            Long currentUserId = userService.currentLoginUser().getId();
            return courseRepository.findByCreatorId(currentUserId, pageable);
        }
        if (isCurrentStudent) {
            Long currentUserId = userService.currentLoginUser().getId();
            return courseRepository.findByParticipantsId(currentUserId, pageable);

        }
        if (categoryId != null && title != null) {
            return courseRepository.findByCategoryIdAndTitleContaining(categoryId, title, pageable);
        } else if (categoryId != null) {
            return courseRepository.findByCategoryId(categoryId, pageable);
        } else if (title != null) {
            return courseRepository.findByTitleContaining(title, pageable);
        } else {
            return courseRepository.findAll(pageable);
        }
    }

    @Transactional
    @CachePut(cacheNames = "course", key = "#result.id")
    public Course createCourse(Course course) {
        String label = course.getCategory().getLabel();
        Category category = categoryRepository.findByLabel(label)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with label: " + label));
        course.setCategory(category);
        course.setCreator(userService.currentLoginUser());
        String address = course.getAddress().getStreet() + ", " + course.getAddress().getStreetNumber() + ", " +
                course.getAddress().getApartmentNumber() + ", " + course.getAddress().getCity() + ", " + course.getAddress().getPostalCode();
        LatLng latLng = geoCodingService.getGeoCoding(address);
        if (null != latLng) {
            course.getAddress().setLat(latLng.lat);
            course.getAddress().setLng(latLng.lng);
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
        return courseRepository.findByStartDateBetweenOrEndDateBetween(
                LocalDateTime.of(from, LocalTime.MIN),
                LocalDateTime.of(from, LocalTime.MAX),
                LocalDateTime.of(to, LocalTime.MIN),
                LocalDateTime.of(to, LocalTime.MAX));
    }
}

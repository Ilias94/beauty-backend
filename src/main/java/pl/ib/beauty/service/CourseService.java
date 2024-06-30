package pl.ib.beauty.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.repository.CourseRepository;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public Page<Course> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @Transactional
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }


    @Transactional
    public Course updateCourse(Long id, Course updatedCourse) {
        Course existingCourse = courseRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        existingCourse.setTitle(updatedCourse.getTitle());
        existingCourse.setDescription(updatedCourse.getDescription());
        existingCourse.setStartDate(updatedCourse.getStartDate());
        existingCourse.setEndDate(updatedCourse.getEndDate());
        existingCourse.setMaxParticipants(updatedCourse.getMaxParticipants());
        existingCourse.setAddress(updatedCourse.getAddress());
        existingCourse.setCategory(updatedCourse.getCategory());
        return courseRepository.save(existingCourse);
    }

    @Transactional
    public void deleteCourseById(Long id) {
        courseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));
    }
}

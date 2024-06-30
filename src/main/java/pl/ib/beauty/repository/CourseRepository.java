package pl.ib.beauty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ib.beauty.model.dao.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
}

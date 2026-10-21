package pl.ib.beauty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ib.beauty.model.dao.CourseImage;

import java.util.List;

public interface CourseImageRepository extends JpaRepository<CourseImage, Long> {
    List<CourseImage> findByCourseIdOrderByPositionAsc(Long courseId);
}

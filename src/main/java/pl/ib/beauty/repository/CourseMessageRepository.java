package pl.ib.beauty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.ib.beauty.model.dao.CourseMessage;

import java.util.List;

public interface CourseMessageRepository extends JpaRepository<CourseMessage, Long> {

    @Query("SELECT m FROM CourseMessage m JOIN FETCH m.sender WHERE m.course.id = :courseId ORDER BY m.sentAt ASC")
    List<CourseMessage> findByCourseId(@Param("courseId") Long courseId);
}

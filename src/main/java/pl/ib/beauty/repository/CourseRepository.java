package pl.ib.beauty.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.ib.beauty.model.dao.Category;
import pl.ib.beauty.model.dao.Course;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByCategoryIdAndTitleContaining(Long categoryId, String title, Pageable pageable);

    Page<Course> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Course> findByCreatorId(Long creatorId, Pageable pageable);

    Page<Course> findByParticipantsId(Long participantId, Pageable pageable);

    Page<Course> findByTitleContaining(String title, Pageable pageable);

    List<Course> findByTitleContainingIgnoreCase(String title);
}

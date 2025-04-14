package pl.ib.beauty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ib.beauty.model.dao.Comment;
import pl.ib.beauty.model.dao.Rating;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByCourseIdAndAuthorId(Long courseId, Long authorId);
    List<Rating> findByCourseId(Long courseId);
}

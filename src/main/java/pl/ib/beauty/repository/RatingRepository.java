package pl.ib.beauty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.ib.beauty.model.dao.Rating;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByCourseIdAndAuthorId(Long courseId, Long authorId);
    List<Rating> findByCourseId(Long courseId);

    @Query("SELECT AVG(r.instructorRating) FROM Rating r WHERE r.course.creator.id = :instructorId AND r.instructorRating IS NOT NULL")
    Optional<Double> findAverageInstructorRating(@Param("instructorId") Long instructorId);
}

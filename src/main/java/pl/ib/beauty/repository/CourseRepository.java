package pl.ib.beauty.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.ib.beauty.model.dao.Course;

import java.time.LocalDateTime;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    Page<Course> findByCategoryIdAndTitleContaining(Long categoryId, String title, Pageable pageable);

    Page<Course> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Course> findByCreatorId(Long creatorId, Pageable pageable);

    Page<Course> findByParticipantsId(Long participantId, Pageable pageable);

    Page<Course> findByTitleContaining(String title, Pageable pageable);

    List<Course> findByTitleContainingIgnoreCase(String title);


    @Query("""
            SELECT c FROM Course c
            WHERE c.creator.id = :creatorId
            AND c.startDate <= :to
            AND c.endDate >= :from
            """)
    List<Course> findCoursesOverlapping(
            @Param("creatorId") Long creatorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("SELECT c FROM Course c WHERE (c.endDate IS NULL OR c.endDate >= :now)")
    Page<Course> findAllActive(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.category.id = :categoryId AND (c.endDate IS NULL OR c.endDate >= :now)")
    Page<Course> findActiveByCategoryId(@Param("categoryId") Long categoryId, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%')) AND (c.endDate IS NULL OR c.endDate >= :now)")
    Page<Course> findActiveByTitleContaining(@Param("title") String title, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.category.id = :categoryId AND LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%')) AND (c.endDate IS NULL OR c.endDate >= :now)")
    Page<Course> findActiveByCategoryIdAndTitleContaining(@Param("categoryId") Long categoryId, @Param("title") String title, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT DISTINCT addr.city FROM Course c JOIN c.address addr WHERE addr.city IS NOT NULL AND addr.city <> '' ORDER BY addr.city")
    List<String> findDistinctCities();

}

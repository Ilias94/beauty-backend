package pl.ib.beauty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ib.beauty.model.dao.Video;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByCourseId(Long courseId);
}

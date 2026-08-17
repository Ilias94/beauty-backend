package pl.ib.beauty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ib.beauty.model.dao.Question;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByCourseId(Long courseId);


}

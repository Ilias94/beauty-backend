package pl.ib.beauty.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.Question;
import pl.ib.beauty.repository.CourseRepository;
import pl.ib.beauty.repository.QuestionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final CourseRepository courseRepository;

    // CREATE
    public List<Question> create(List<Question> questions, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("No course with id: " + courseId));
        questions.forEach(question -> question.setCourse(course));
        return questionRepository.saveAll(questions);
    }

    // READ - by id
    public Question getById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found, id: " + id));
    }


    // READ - by course
    public List<Question> getByCourse(Long courseId) {
        return questionRepository.findByCourseId(courseId);
    }

    // UPDATE
    public Question update(Long courseId, Question updated) {
        Question existing = getById(courseId);

        existing.setCourse(updated.getCourse());
        existing.setPriority(updated.getPriority());
        existing.setQuestionDetail(updated.getQuestionDetail());

        return questionRepository.save(existing);
    }

    // DELETE
    public void delete(Long id) {
        questionRepository.deleteById(id);
    }
}

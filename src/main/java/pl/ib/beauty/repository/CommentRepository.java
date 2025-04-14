package pl.ib.beauty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ib.beauty.model.dao.Comment;

import java.util.List;


public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> getCommentByCourseId(Long courseId);
    List<Comment> findCommentByAuthorId(Long authorId);
}

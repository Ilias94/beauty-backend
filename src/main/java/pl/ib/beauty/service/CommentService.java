package pl.ib.beauty.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.ib.beauty.model.dao.Comment;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.repository.CommentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CourseService courseService;
    private final UserService userService;

    public Comment saveComment(String content, Long courseId, Long parentId) {
        Course courseById = courseService.getCourseById(courseId);
        User currentLoginUser = userService.currentLoginUser();

        Comment comment = Comment.builder()
                .course(courseById)
                .author(currentLoginUser)
                .content(content)
                .build();
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByCourseId(Long courseId) {
        return commentRepository.getCommentByCourseId(courseId);
    }

    public List<Comment> getCommentsByAuthorId() {
        User currentLoginUser = userService.currentLoginUser();
        return commentRepository.findCommentByAuthorId(currentLoginUser.getId());
    }
}

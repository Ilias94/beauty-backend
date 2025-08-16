package pl.ib.beauty.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pl.ib.beauty.mapper.CommentMapper;
import pl.ib.beauty.model.dto.CommentDtoRequest;
import pl.ib.beauty.model.dto.CommentDtoResponse;
import pl.ib.beauty.service.CommentService;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/comment", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final CommentMapper commentMapper;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDtoResponse createComment(@RequestBody CommentDtoRequest commentDtoRequest) {
        return commentMapper.commentToCommentDto(commentService.saveComment(commentDtoRequest.content(), commentDtoRequest.courseId(), commentDtoRequest.parentId()));
    }

    @GetMapping("{courseId}")
    public List<CommentDtoResponse> getComments(@PathVariable Long courseId) {
        return commentMapper.commentsToCommentsDto(commentService.getCommentsByCourseId(courseId));
    }

    @GetMapping("/current")
    public List<CommentDtoResponse> getCurrentUserComments() {
        return commentMapper.commentsToCommentsDto(commentService.getCommentsByAuthorId());
    }
}

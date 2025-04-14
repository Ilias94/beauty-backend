package pl.ib.beauty.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pl.ib.beauty.mapper.CommentMapper;
import pl.ib.beauty.model.dto.CommentDto;
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
    public CommentDto createComment(@RequestBody CommentDto commentDto) {
        return commentMapper.commentToCommentDto(commentService.saveComment(commentDto.getContent(), commentDto.getCourseId(), commentDto.getParentId()));
    }

    @GetMapping("{courseId}")
    public List<CommentDto> getComments(@PathVariable Long courseId) {
        return commentMapper.commentsToCommentsDto(commentService.getCommentsByCourseId(courseId));
    }

    @GetMapping("/current")
    public List<CommentDto> getCurrentUserComments() {
        return commentMapper.commentsToCommentsDto(commentService.getCommentsByAuthorId());
    }
}

package pl.ib.beauty.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pl.ib.beauty.model.dto.CourseMessageDtoRequest;
import pl.ib.beauty.model.dto.CourseMessageDtoResponse;
import pl.ib.beauty.service.CourseMessageService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/courses/{courseId}/messages", produces = MediaType.APPLICATION_JSON_VALUE)
public class CourseMessageController {

    private final CourseMessageService courseMessageService;

    @GetMapping
    public List<CourseMessageDtoResponse> getMessages(@PathVariable Long courseId) {
        return courseMessageService.getMessages(courseId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseMessageDtoResponse send(@PathVariable Long courseId,
                                         @RequestBody @Valid CourseMessageDtoRequest request) {
        return courseMessageService.send(courseId, request);
    }
}

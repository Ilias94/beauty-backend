package pl.ib.beauty.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pl.ib.beauty.mapper.QuestionMapper;
import pl.ib.beauty.model.dto.QuestionDtoRequest;
import pl.ib.beauty.model.dto.QuestionDtoResponse;
import pl.ib.beauty.repository.CourseRepository;
import pl.ib.beauty.repository.QuestionRepository;
import pl.ib.beauty.service.QuestionService;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/questions", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionRepository questionRepository;
    private final CourseRepository courseRepository;
    private final QuestionService questionService;
    private final QuestionMapper questionMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // A quiz is its course's instructor's to write, and only enrolled people (and the instructor) may read it.
    @PreAuthorize("isAuthenticated() and (@securityService.isCourseCreator(#questionRequest.courseId()) or hasAuthority('SCOPE_ADMIN'))")
    public void create(@RequestBody QuestionDtoRequest questionRequest) {
        questionService.create(questionMapper.toQuestion(questionRequest.questionDetail()), questionRequest.courseId());
    }

//    @PutMapping
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void update(@RequestBody QuestionDtoRequest questionRequest) {
//        questionService.update(questionRequest.courseId(), questionMapper.toQuestion(questionRequest.questionDetail()));
//    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated() and (@securityService.canAccessCourseMaterials(#courseId) or hasAuthority('SCOPE_ADMIN'))")
    public List<QuestionDtoResponse> findQuestionsByCourseId(@RequestParam Long courseId) {
        return questionMapper.toResponses(questionService.getByCourse(courseId));
    }

    @DeleteMapping("/{id}")
    @Operation(operationId = "delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated() and (@securityService.isQuestionOwner(#id) or hasAuthority('SCOPE_ADMIN'))")
    public void delete(@PathVariable Long id) {
        questionService.delete(id);
    }
}

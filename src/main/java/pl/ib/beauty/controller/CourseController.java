package pl.ib.beauty.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.ib.beauty.mapper.CourseMapper;
import pl.ib.beauty.model.dto.CourseDto;
import pl.ib.beauty.service.CourseService;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/courses", produces = MediaType.APPLICATION_JSON_VALUE)
public class CourseController {

    private final CourseService courseService;
    private final CourseMapper courseMapper;

    @GetMapping
    public Page<CourseDto> getCourses(@RequestParam int page, @RequestParam int size) {
        return courseService.getAllCourses(PageRequest.of(page, size)).map(courseMapper::courseToDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SCOPE_TEACHER') or hasRole('ROLE_ADMIN')")
    public CourseDto createCourse(@RequestBody @Valid CourseDto courseDto) {
        var course = courseMapper.dtoToCourse(courseDto);
        return courseMapper.courseToDto(courseService.createCourse(course));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_TEACHER') and @securityService.isCourseCreator(#id) or hasAuthority('SCOPE_ADMIN')")
    public CourseDto updateCourse(@PathVariable Long id, @RequestBody @Valid CourseDto courseDto) {
        var updatedCourse = courseMapper.dtoToCourse(courseDto);
        return courseMapper.courseToDto(courseService.updateCourse(id, updatedCourse));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteCourse(@PathVariable Long id) {
        courseService.deleteCourseById(id);
    }

    @GetMapping("/{id}")
    public CourseDto getCourseById(@PathVariable Long id) {
        return courseMapper.courseToDto(courseService.getCourseById(id));
    }
}

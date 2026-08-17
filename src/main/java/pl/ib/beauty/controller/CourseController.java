package pl.ib.beauty.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.ib.beauty.mapper.CourseMapper;
import pl.ib.beauty.mapper.PageableMapper;
import pl.ib.beauty.model.CourseType;
import pl.ib.beauty.model.dto.CourseDtoRequest;
import pl.ib.beauty.model.dto.CourseDtoResponse;
import pl.ib.beauty.model.dto.RepublishCourseRequest;
import pl.ib.beauty.service.CourseService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/courses", produces = MediaType.APPLICATION_JSON_VALUE)
public class CourseController {

    private final CourseService courseService;
    private final CourseMapper courseMapper;
    private final PageableMapper pageableMapper;

    @GetMapping
    public Page<CourseDtoResponse> getCourses(@RequestParam(required = false, defaultValue = "0") int page,
                                              @RequestParam(required = false, defaultValue = "20") int size,
                                              @RequestParam(required = false) Long categoryId,
                                              @RequestParam(required = false) String title,
                                              @RequestParam(required = false) String city,
                                              @RequestParam(required = false) CourseType courseType,
                                              @RequestParam(required = false, defaultValue = "startDate") String sortBy,
                                              @RequestParam(required = false) Sort.Direction sortDirection,
                                              @RequestParam(required = false) Boolean isCurrentCreator,
                                              @RequestParam(required = false) Boolean isCurrentStudent,
                                              @RequestParam(required = false, defaultValue = "true") boolean hidePast) {
        Pageable pageable = pageableMapper.createPageable(page, size, sortDirection, sortBy);
        boolean creator = Boolean.TRUE.equals(isCurrentCreator);
        boolean student = Boolean.TRUE.equals(isCurrentStudent);
        return courseService.getCoursesByCategoryAndTitle(categoryId, title, city, courseType, pageable, creator, student, hidePast)
                .map(courseMapper::courseToDto);
    }

    @GetMapping("/cities")
    public List<String> getDistinctCities() {
        return courseService.getDistinctCities();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SCOPE_TEACHER') or hasRole('ROLE_ADMIN')")
    public CourseDtoResponse createCourse(@RequestBody @Valid CourseDtoRequest courseDto) {
        var course = courseMapper.dtoToCourse(courseDto);
        return courseMapper.courseToDto(courseService.createCourse(course));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_TEACHER') and @securityService.isCourseCreator(#id) or hasAuthority('SCOPE_ADMIN')")
    public CourseDtoResponse updateCourse(@PathVariable Long id, @RequestBody @Valid CourseDtoRequest courseDto) {
        var course = courseMapper.dtoToCourse(courseDto);
        return courseMapper.courseToDto(courseService.updateCourse(id, course));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or @securityService.isCourseCreator(#id)")
    public void deleteCourse(@PathVariable Long id) {
        courseService.deleteCourseById(id);
    }

    @GetMapping("/{id}")
    public CourseDtoResponse getCourseById(@PathVariable Long id) {
        return courseMapper.courseToDto(courseService.getCourseById(id));
    }

    @PostMapping("/{id}/republish")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SCOPE_TEACHER') and @securityService.isCourseCreator(#id) or hasAuthority('SCOPE_ADMIN')")
    public CourseDtoResponse republishCourse(@PathVariable Long id, @RequestBody RepublishCourseRequest request) {
        return courseMapper.courseToDto(courseService.republishCourse(id, request));
    }

    @GetMapping("/autocomplete")
    public List<String> getAutocompleteTitle(@RequestParam String title) {
        return courseService.getAutocompleteTitle(title);
    }

    @GetMapping("/dates-filter")
    public List<CourseDtoResponse> filterCoursesByDate(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        return courseMapper.toDtoList(courseService.findByDate(from, to));
    }
}


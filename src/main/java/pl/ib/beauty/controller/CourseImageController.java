package pl.ib.beauty.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.ib.beauty.model.dto.CourseImageDto;
import pl.ib.beauty.service.CourseImageService;

import java.util.List;

/** A course's photo gallery: anyone can see it, only the course's creator (or an admin) changes it. */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/courses/{courseId}/images", produces = MediaType.APPLICATION_JSON_VALUE)
public class CourseImageController {
    private static final String CAN_EDIT = "isAuthenticated() and (@securityService.isCourseCreator(#courseId) or hasAuthority('SCOPE_ADMIN'))";

    private final CourseImageService courseImageService;

    @GetMapping
    public List<CourseImageDto> getCourseImages(@PathVariable Long courseId) {
        return courseImageService.getImages(courseId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(CAN_EDIT)
    public List<CourseImageDto> addCourseImages(@PathVariable Long courseId, @RequestPart("files") List<MultipartFile> files) {
        return courseImageService.addImages(courseId, files);
    }

    @PutMapping("/order")
    @PreAuthorize(CAN_EDIT)
    public List<CourseImageDto> reorderCourseImages(@PathVariable Long courseId, @RequestBody List<Long> imageIds) {
        return courseImageService.reorder(courseId, imageIds);
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize(CAN_EDIT)
    public List<CourseImageDto> deleteCourseImage(@PathVariable Long courseId, @PathVariable Long imageId) {
        return courseImageService.delete(courseId, imageId);
    }
}

package pl.ib.beauty.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.ib.beauty.model.dto.VideoDescriptionEditRequest;
import pl.ib.beauty.model.dto.VideoDtoResponse;
import pl.ib.beauty.service.VideoService;
import pl.ib.beauty.validator.impl.ExtensionValid;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/video", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class VideoController {
    private final VideoService videoService;

    /** Signed links to the recordings: the course's creator and enrolled people only (and admins). */
    @GetMapping("/{courseId}")
    @PreAuthorize("isAuthenticated() and (@securityService.canAccessCourseMaterials(#courseId) or hasAuthority('SCOPE_ADMIN'))")
    public List<VideoDtoResponse> getVideos(@PathVariable Long courseId) {
        return videoService.getVideosForCourse(courseId);
    }

    @PostMapping(value = "/upload/{courseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated() and (@securityService.isCourseCreator(#courseId) or hasAuthority('SCOPE_ADMIN'))")
    public void uploadVideos(@PathVariable Long courseId,
                             @RequestParam @Valid List<@ExtensionValid(supportedExtensions = {"mp4", "avi"}) MultipartFile> videos) {
        videoService.uploadVideos(courseId, videos);
    }

    @PatchMapping("/description")
    @PreAuthorize("isAuthenticated() and (@securityService.isVideoOwner(#videoDescriptionEditRequest.videoId()) or hasAuthority('SCOPE_ADMIN'))")
    public void editDescription(@RequestBody VideoDescriptionEditRequest videoDescriptionEditRequest) {
        videoService.addVideoDescription(videoDescriptionEditRequest.videoId(), videoDescriptionEditRequest.description());
    }

    @DeleteMapping("/{videoId}")
    @PreAuthorize("isAuthenticated() and (@securityService.isVideoOwner(#videoId) or hasAuthority('SCOPE_ADMIN'))")
    public void deleteVideo(@PathVariable Long videoId) {
        videoService.deleteVideo(videoId);
    }
}

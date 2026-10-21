package pl.ib.beauty.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.CourseImage;
import pl.ib.beauty.model.dto.CourseImageDto;
import pl.ib.beauty.repository.CourseImageRepository;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * A course's photo gallery: up to {@link #MAX_IMAGES} pictures in the public bucket, in the order the
 * creator set. The first one is the cover, mirrored to {@code course.imageUrl} for the course lists.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseImageService {
    public static final int MAX_IMAGES = 8;

    private final CourseImageRepository courseImageRepository;
    private final CourseService courseService;
    private final FileService fileService;

    public List<CourseImageDto> getImages(Long courseId) {
        return courseImageRepository.findByCourseIdOrderByPositionAsc(courseId).stream().map(this::toDto).toList();
    }

    @Transactional
    public List<CourseImageDto> addImages(Long courseId, List<MultipartFile> files) {
        List<CourseImage> images = courseImageRepository.findByCourseIdOrderByPositionAsc(courseId);
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No photos sent");
        }
        if (images.size() + files.size() > MAX_IMAGES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A course can have at most " + MAX_IMAGES + " photos");
        }

        // Check every file before storing any, so a rejected one leaves nothing behind in the bucket.
        List<byte[]> contents = files.stream().map(CourseImageService::read).toList();
        List<ImageType> types = contents.stream().map(ImageType::of).toList();

        Course course = Course.builder().id(courseId).build();
        int position = images.size();
        for (int i = 0; i < contents.size(); i++) {
            ImageType type = types.get(i);
            String key = "courses/" + courseId + "/" + UUID.randomUUID() + "." + type.extension;
            fileService.saveFile(key, contents.get(i), type.contentType);
            courseImageRepository.save(CourseImage.builder().course(course).fileName(key).position(position++).build());
        }
        return syncCover(courseId);
    }

    /** {@code imageIds} is the whole gallery in its new order; the first becomes the cover. */
    @Transactional
    public List<CourseImageDto> reorder(Long courseId, List<Long> imageIds) {
        List<CourseImage> images = courseImageRepository.findByCourseIdOrderByPositionAsc(courseId);
        if (imageIds.size() != images.size()
                || !new HashSet<>(imageIds).equals(new HashSet<>(images.stream().map(CourseImage::getId).toList()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The order must list every photo of the course once");
        }
        images.forEach(image -> image.setPosition(imageIds.indexOf(image.getId())));
        courseImageRepository.saveAll(images);
        return syncCover(courseId);
    }

    @Transactional
    public List<CourseImageDto> delete(Long courseId, Long imageId) {
        CourseImage image = courseImageRepository.findById(imageId)
                .filter(i -> i.getCourse().getId().equals(courseId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found in this course"));
        courseImageRepository.delete(image);
        try {
            fileService.deleteFile(image.getFileName());
        } catch (RuntimeException e) {
            // The gallery no longer points at it; a leftover object in the bucket is harmless.
            log.warn("Could not delete {} from the bucket", image.getFileName(), e);
        }

        List<CourseImage> rest = courseImageRepository.findByCourseIdOrderByPositionAsc(courseId);
        for (int i = 0; i < rest.size(); i++) {
            rest.get(i).setPosition(i);
        }
        courseImageRepository.saveAll(rest);
        return syncCover(courseId);
    }

    private List<CourseImageDto> syncCover(Long courseId) {
        List<CourseImage> images = courseImageRepository.findByCourseIdOrderByPositionAsc(courseId);
        courseService.updateCover(courseId, images.isEmpty() ? null : images.getFirst().getFileName());
        return images.stream().map(this::toDto).toList();
    }

    private CourseImageDto toDto(CourseImage image) {
        return new CourseImageDto(image.getId(), image.getFileName(), image.getPosition());
    }

    private static byte[] read(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read the photo", e);
        }
    }

    /**
     * The picture's real format, from its first bytes rather than its name or the browser's claim,
     * since the bucket serves it publicly with this content type.
     */
    enum ImageType {
        JPEG("jpg", "image/jpeg"), PNG("png", "image/png"), WEBP("webp", "image/webp"), AVIF("avif", "image/avif");

        final String extension;
        final String contentType;

        ImageType(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }

        static ImageType of(byte[] b) {
            if (b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return JPEG;
            if (b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return PNG;
            if (b.length > 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return WEBP;
            if (b.length > 12 && b[4] == 'f' && b[5] == 't' && b[6] == 'y' && b[7] == 'p'
                    && b[8] == 'a' && b[9] == 'v' && b[10] == 'i' && b[11] == 'f') return AVIF;
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Photos must be JPEG, PNG, WebP or AVIF");
        }
    }
}

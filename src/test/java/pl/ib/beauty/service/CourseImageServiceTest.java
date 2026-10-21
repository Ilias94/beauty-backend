package pl.ib.beauty.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.CourseImage;
import pl.ib.beauty.model.dto.CourseImageDto;
import pl.ib.beauty.repository.CourseImageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourseImageServiceTest {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P'};
    private static final byte[] TEXT = "<svg onload=alert(1)>".getBytes();

    private final CourseImageRepository repository = mock(CourseImageRepository.class);
    private final CourseService courseService = mock(CourseService.class);
    private final FileService fileService = mock(FileService.class);
    private final CourseImageService service = new CourseImageService(repository, courseService, fileService);

    /** The repository's gallery for course 16, kept in a list so saves show up in later reads. */
    private final List<CourseImage> stored = new ArrayList<>();

    CourseImageServiceTest() {
        when(repository.findByCourseIdOrderByPositionAsc(16L)).thenAnswer(inv ->
                stored.stream().sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition())).toList());
        when(repository.save(any(CourseImage.class))).thenAnswer(inv -> {
            CourseImage image = inv.getArgument(0);
            image.setId((long) stored.size() + 1);
            stored.add(image);
            return image;
        });
    }

    @Test
    void storesPhotosUnderTheCourseWithTheirRealTypeAndSetsTheCover() {
        List<CourseImageDto> result = service.addImages(16L, List.of(file("a.jpg", JPEG), file("b.png", WEBP)));

        assertThat(result).extracting(CourseImageDto::position).containsExactly(0, 1);
        assertThat(result.get(1).fileName()).startsWith("courses/16/").endsWith(".webp");
        verify(fileService).saveFile(startsWith("courses/16/"), eq(JPEG), eq("image/jpeg"));
        verify(fileService).saveFile(anyString(), eq(WEBP), eq("image/webp"));
        verify(courseService).updateCover(16L, result.getFirst().fileName());
    }

    @Test
    void rejectsAFileThatIsNotAPictureBeforeStoringAnything() {
        assertThatThrownBy(() -> service.addImages(16L, List.of(file("a.jpg", JPEG), file("evil.jpg", TEXT))))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE));
        verify(fileService, never()).saveFile(anyString(), any(), anyString());
    }

    @Test
    void keepsAGalleryToEightPhotos() {
        IntStream.range(0, 7).forEach(i -> stored.add(image((long) i + 1, i)));

        assertThatThrownBy(() -> service.addImages(16L, List.of(file("a.jpg", JPEG), file("b.jpg", JPEG))))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void reorderingMakesTheFirstPhotoTheCover() {
        stored.add(image(1L, 0));
        stored.add(image(2L, 1));
        stored.add(image(3L, 2));

        List<CourseImageDto> result = service.reorder(16L, List.of(3L, 1L, 2L));

        assertThat(result).extracting(CourseImageDto::id).containsExactly(3L, 1L, 2L);
        verify(courseService).updateCover(16L, "courses/16/3.webp");
    }

    @Test
    void reorderMustNameEveryPhotoOnce() {
        stored.add(image(1L, 0));
        stored.add(image(2L, 1));

        assertThatThrownBy(() -> service.reorder(16L, List.of(1L, 1L)))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service.reorder(16L, List.of(1L, 2L, 99L)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deletingThePhotoOfAnotherCourseIsNotFound() {
        CourseImage other = image(5L, 0);
        other.setCourse(Course.builder().id(99L).build());
        when(repository.findById(5L)).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.delete(16L, 5L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
        verify(fileService, never()).deleteFile(anyString());
    }

    @Test
    void deletingTheCoverPromotesTheNextPhotoAndClearsWhenNoneIsLeft() {
        CourseImage cover = image(1L, 0);
        CourseImage second = image(2L, 1);
        stored.add(cover);
        stored.add(second);
        when(repository.findById(anyLong())).thenAnswer(inv -> stored.stream().filter(i -> i.getId().equals(inv.getArgument(0))).findFirst());
        org.mockito.Mockito.doAnswer(inv -> stored.remove(inv.<CourseImage>getArgument(0))).when(repository).delete(any(CourseImage.class));

        List<CourseImageDto> afterFirst = service.delete(16L, 1L);
        assertThat(afterFirst).extracting(CourseImageDto::id, CourseImageDto::position).containsExactly(org.assertj.core.groups.Tuple.tuple(2L, 0));
        verify(courseService).updateCover(16L, "courses/16/2.webp");
        verify(fileService).deleteFile("courses/16/1.webp");

        service.delete(16L, 2L);
        verify(courseService).updateCover(16L, null);
    }

    private static CourseImage image(Long id, int position) {
        return CourseImage.builder().id(id).course(Course.builder().id(16L).build())
                .fileName("courses/16/" + id + ".webp").position(position).build();
    }

    private static MultipartFile file(String name, byte[] content) {
        return new MockMultipartFile("files", name, "image/jpeg", content);
    }
}

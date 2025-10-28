package pl.ib.beauty.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.Video;
import pl.ib.beauty.model.dto.VideoDtoResponse;
import pl.ib.beauty.repository.VideoRepository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoRepository videoRepository;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;


    public List<VideoDtoResponse> getVideosForCourse(Long courseId) {
        List<Video> videos = videoRepository.findByCourseId(courseId);

        return videos.stream().map(video -> {
            // Tworzymy request do S3
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket("beautyapplicationvideo") // 🔒 nazwa bucketu
                    .key(video.getPath())             // klucz pliku w S3
                    .build();

            // Tworzymy pre-signed URL ważny 10 min
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

            return VideoDtoResponse.builder()
                    .url(presignedRequest.url().toString())
                    .title(video.getTitle())
                    .description(video.getDescription())
                    .id(video.getId())
                    .build();
        }).collect(Collectors.toList());
    }

    @SneakyThrows
    private void createVideo(Long courseId, MultipartFile file) {
        String key = "courses/" + courseId + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        // 1. Zapisz metadane w DB
        Video video = Video.builder()
                .path(key)
                .title(FilenameUtils.getName(file.getOriginalFilename()))
                .course(Course.builder().id(courseId).build())
                .build();
        videoRepository.save(video);

        // 2. Wygeneruj pre-signed URL (PUT) do wrzucenia pliku na S3
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket("beautyapplicationvideo")
                .key(key)
                .build();


        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
    }

    public void uploadVideos(Long courseId, List<MultipartFile> videos) {
        for (MultipartFile video : videos) {
            createVideo(courseId, video);
        }
    }

    public void addVideoDescription(Long videoId, String description) {
        Optional<Video> optionalVideo = videoRepository.findById(videoId);
        Video video = optionalVideo.orElseThrow(() -> new EntityNotFoundException("Video with id: " + videoId + " not found"));
        video.setDescription(description);
        videoRepository.save(video);
    }
}

package pl.ib.beauty.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class FileService {
    private static final String PICTURES_BUCKET = "picturesbeautyapplication";

    private final S3Client s3Client;

    public void saveFile(String s3Key, byte[] file) {
        saveFile(s3Key, file, null);
    }

    /** With a content type the public bucket serves the file as that type instead of binary/octet-stream. */
    public void saveFile(String s3Key, byte[] file, String contentType) {
        PutObjectRequest pictureRequest = PutObjectRequest.builder()
                .bucket(PICTURES_BUCKET)
                .key(s3Key)
                .contentType(contentType)
                .build();
        s3Client.putObject(pictureRequest, RequestBody.fromBytes(file));
    }

    public void deleteFile(String s3Key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(PICTURES_BUCKET)
                .key(s3Key)
                .build());
    }
}

package pl.ib.beauty.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class FileService {
    private final S3Client s3Client;

    public void saveFile(String s3Key, byte[] file) {
        PutObjectRequest pictureRequest = PutObjectRequest.builder()
                .bucket("picturesbeautyapplication")
                .key(s3Key)
                .build();
        s3Client.putObject(pictureRequest, RequestBody.fromBytes(file));
    }
}

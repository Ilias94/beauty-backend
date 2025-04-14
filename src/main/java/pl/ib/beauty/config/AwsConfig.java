package pl.ib.beauty.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    @Bean
    public S3Client s3Client(AwsCredentialConfig awsCredentialConfig) {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(awsCredentialConfig.getAccessKey(), awsCredentialConfig.getSecretKey());
        return S3Client.builder()
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .build();
    }
}

package pl.ib.beauty.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "aws")
@Configuration
@Getter
@Setter
public class AwsCredentialConfig {
    private String accessKey;
    private String secretKey;
}

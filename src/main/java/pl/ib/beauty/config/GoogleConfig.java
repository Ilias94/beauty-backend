package pl.ib.beauty.config;

import com.google.maps.GeoApiContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleConfig {

    @Bean
    public GeoApiContext geoApiContext(GooglePropertiesConfig googlePropertiesConfig) {
        return new GeoApiContext.Builder()
                .apiKey(googlePropertiesConfig.getAccessKey())
                .build();
    }
}

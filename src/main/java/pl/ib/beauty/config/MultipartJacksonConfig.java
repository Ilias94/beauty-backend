package pl.ib.beauty.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractJacksonHttpMessageConverter;
import tools.jackson.databind.json.JsonMapper;

/** Reads JSON multipart parts sent as application/octet-stream (read-only: never used to write responses). */
@Configuration
public class MultipartJacksonConfig extends AbstractJacksonHttpMessageConverter<JsonMapper> {
    protected MultipartJacksonConfig(JsonMapper jsonMapper) {
        super(jsonMapper, MediaType.APPLICATION_OCTET_STREAM);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(ResolvableType type, Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    protected boolean canWrite(MediaType mediaType) {
        return false;
    }
}

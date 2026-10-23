package pl.ib.beauty.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidPasswordExceptionTest {

    @Test
    void aWrongOldPasswordIsTheUsersMistakeNotAServerError() {
        ResponseStatus status = AnnotatedElementUtils.findMergedAnnotation(InvalidPasswordException.class, ResponseStatus.class);

        assertThat(status).isNotNull();
        assertThat(status.value()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}

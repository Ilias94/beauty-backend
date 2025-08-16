package pl.ib.beauty.validator.impl;

import org.junit.jupiter.api.Test;
import pl.ib.beauty.model.dto.UserDtoRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordValidatorTest {
    private PasswordValidator passwordValidator = new PasswordValidator();

    @Test
    void shouldValidPassword() {
        //given
        UserDtoRequest user = UserDtoRequest.builder()
                .password("password")
                .confirmPassword("password")
                .build();

        //when
        boolean result = passwordValidator.isValid(user, null);

        //then
        assertTrue(result);
    }

    @Test
    void shouldNotValidPassword() {
        //given
        UserDtoRequest user = UserDtoRequest.builder()
                .password("password")
                .confirmPassword("password2")
                .build();

        //when
        boolean result = passwordValidator.isValid(user, null);

        //then
        assertFalse(result);
    }
}
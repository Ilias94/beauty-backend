package pl.ib.beauty.validator.impl;

import org.junit.jupiter.api.Test;
import pl.ib.beauty.model.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {
    private PasswordValidator passwordValidator = new PasswordValidator();

    @Test
    void shouldValidPassword() {
        //given
        UserDto user = UserDto.builder()
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
        UserDto user = UserDto.builder()
                .password("password")
                .confirmPassword("password2")
                .build();

        //when
        boolean result = passwordValidator.isValid(user, null);

        //then
        assertFalse(result);
    }
}
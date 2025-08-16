package pl.ib.beauty.validator.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.ib.beauty.model.dto.UserDtoRequest;
import pl.ib.beauty.validator.PasswordValid;

public class PasswordValidator implements ConstraintValidator<PasswordValid, UserDtoRequest> {
    @Override
    public boolean isValid(UserDtoRequest userDto, ConstraintValidatorContext constraintValidatorContext) {
        return userDto.password().equals(userDto.confirmPassword());
    }
}

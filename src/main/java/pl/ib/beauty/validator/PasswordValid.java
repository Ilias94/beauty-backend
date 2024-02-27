package pl.ib.beauty.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import pl.ib.beauty.validator.impl.PasswordValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordValid {
    String message() default "Unmatched password";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

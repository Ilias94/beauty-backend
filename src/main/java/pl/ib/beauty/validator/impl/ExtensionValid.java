package pl.ib.beauty.validator.impl;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ExtensionValidator.class)
@Target({ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtensionValid {
    String message() default "Wrong file extension";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] supportedExtensions() default {};
}

package com.yasirkhan.auth.utils.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UserRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUserRequest {
    String message() default "Invalid request payload based on User Role";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
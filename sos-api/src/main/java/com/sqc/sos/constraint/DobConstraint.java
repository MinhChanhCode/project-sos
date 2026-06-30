package com.sqc.sos.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DobValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DobConstraint {
    String message() default "Invalid date of birth";

    int min() default 0;

    int max() default 150;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

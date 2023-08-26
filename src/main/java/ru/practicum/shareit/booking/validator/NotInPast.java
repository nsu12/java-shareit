package ru.practicum.shareit.booking.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotInPastValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotInPast {
    String message() default "{Date must not be in past}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

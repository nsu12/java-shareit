package ru.practicum.shareit.booking.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class NotInPastValidator implements ConstraintValidator<NotInPast, LocalDateTime> {
    @Override
    public void initialize(NotInPast constraintAnnotation) {
    }

    @Override
    public boolean isValid(LocalDateTime date, ConstraintValidatorContext ctx) {
        return !date.isBefore(LocalDateTime.now());
    }
}

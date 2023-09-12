package ru.practicum.shareit.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

@Slf4j
@ControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(final ValidationException e) {
        log.error("Ошибка валидации, {}", e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.error("Ошибка - некорректные агрументы, {}", e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(InvalidRequestParamsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestParamsException(final InvalidRequestParamsException e) {
        log.error("Некорректные параметры запроса, {}", e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(final ConstraintViolationException e) {
        log.error("Нарушение ограничений, {}", e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }
}

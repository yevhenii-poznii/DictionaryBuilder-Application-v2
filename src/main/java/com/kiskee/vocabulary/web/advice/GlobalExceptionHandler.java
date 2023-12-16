package com.kiskee.vocabulary.web.advice;

import com.kiskee.vocabulary.exception.DuplicateUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private final String logMessage = "[{}] request has received with [{}] at [{}]";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException exception) {
        BindingResult result = exception.getBindingResult();

        List<FieldError> fieldErrors = result.getFieldErrors();

        Map<String, String> errors = fieldErrors.stream()
                .filter(fieldError -> fieldError.getDefaultMessage() != null)
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

        LocalDateTime timestamp = LocalDateTime.now();

        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), errors, timestamp);

        log.info(logMessage, response.getStatus(), errors, timestamp);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUserException(DuplicateUserException exception) {
        String responseMessage = exception.getMessage();

        Map<String, String> errors = Map.of("responseMessage", responseMessage);

        LocalDateTime timestamp = LocalDateTime.now();

        ErrorResponse response = new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), errors,
                timestamp);

        log.info(logMessage, response.getStatus(), errors, timestamp);

        return ResponseEntity.unprocessableEntity().body(response);
    }

}

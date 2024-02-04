package com.kiskee.vocabulary.web.advice;

import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.exception.token.InvalidVerificationTokenException;
import com.kiskee.vocabulary.exception.user.DuplicateUserException;
import com.kiskee.vocabulary.util.TimeZoneContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.time.ZonedDateTime;
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

        ZonedDateTime timestamp = Instant.now().atZone(TimeZoneContextHolder.getTimeZone());

        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), errors, timestamp);

        log.info(logMessage, response.getStatus(), errors, timestamp);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUserException(DuplicateUserException exception) {
        return handleCustomException(exception, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleVerificationTokenNotFoundException(Exception notFoundException) {
        return handleCustomException(notFoundException, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException exception) {
        return handleCustomException(exception, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({InvalidVerificationTokenException.class})
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidVerificationTokenException exception) {
        return handleCustomException(exception, HttpStatus.CONFLICT);
    }

    private ResponseEntity<ErrorResponse> handleCustomException(Throwable exception, HttpStatus status) {
        String responseMessage = exception.getMessage();

        Map<String, String> errors = Map.of("responseMessage", responseMessage);

        ZonedDateTime timestamp = Instant.now().atZone(TimeZoneContextHolder.getTimeZone());

        ErrorResponse response = new ErrorResponse(status.getReasonPhrase(), errors, timestamp);

        log.info(logMessage, response.getStatus(), errors, timestamp);

        return ResponseEntity.status(status).body(response);
    }

}

package com.vladte.devhack.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers.
 * Provides consistent error responses across the API.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation exceptions from @Valid annotations.
     *
     * @param ex the exception
     * @return a map of field errors
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation error",
                errors.toString(),
                LocalDateTime.now()
        );

        log.error("Validation error: {}", errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    /**
     * Handle entity not found exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return an error response
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Entity not found",
                ex.getMessage(),
                LocalDateTime.now()
        );

        log.error("Entity not found: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle response status exceptions.
     *
     * @param ex the exception
     * @return an error response
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getStatusCode().value(),
                "Error",
                ex.getReason(),
                LocalDateTime.now()
        );

        log.error("Response status exception: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }

    /**
     * Handle all other exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return an error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                ex.getMessage(),
                LocalDateTime.now()
        );

        log.error("Unhandled exception", ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

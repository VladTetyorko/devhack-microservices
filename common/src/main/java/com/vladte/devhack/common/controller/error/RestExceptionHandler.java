package com.vladte.devhack.common.controller.error;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers.
 * Provides consistent error responses for different types of exceptions.
 */
@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle validation errors from @Valid annotations.
     *
     * @param ex      the exception
     * @param headers the headers to be written to the response
     * @param status  the selected response status
     * @param request the current request
     * @return a ResponseEntity with validation error details
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                "Validation error",
                ex.getMessage(),
                errors,
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle constraint violation exceptions.
     *
     * @param ex the exception
     * @return a ResponseEntity with validation error details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
        });
        
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                "Constraint violation",
                ex.getMessage(),
                errors,
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle entity not found exceptions.
     *
     * @param ex the exception
     * @return a ResponseEntity with not found error details
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
        log.error("Entity not found: {}", ex.getMessage());
        
        ApiError apiError = new ApiError(
                HttpStatus.NOT_FOUND,
                "Entity not found",
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle data integrity violation exceptions.
     *
     * @param ex the exception
     * @return a ResponseEntity with conflict error details
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());
        
        ApiError apiError = new ApiError(
                HttpStatus.CONFLICT,
                "Data integrity violation",
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    /**
     * Handle access denied exceptions.
     *
     * @param ex the exception
     * @return a ResponseEntity with forbidden error details
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        
        ApiError apiError = new ApiError(
                HttpStatus.FORBIDDEN,
                "Access denied",
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle all other exceptions.
     *
     * @param ex the exception
     * @return a ResponseEntity with internal server error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
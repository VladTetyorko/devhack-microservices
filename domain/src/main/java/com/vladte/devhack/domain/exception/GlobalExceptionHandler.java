package com.vladte.devhack.domain.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced global exception handler for REST controllers.
 * Provides consistent error responses across the API following SOLID and DRY principles.
 *
 * Follows the Single Responsibility Principle by handling only exception-to-response conversion
 * and the Open/Closed Principle by being extensible for new exception types.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle all BaseException and its subclasses.
     * This provides a unified handling approach following DRY principles.
     *
     * @param ex the base exception
     * @return an error response
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        ErrorResponse errorResponse = ErrorResponse.fromException(ex);

        // Log with appropriate level based on exception type
        if (ex instanceof ServiceException) {
            log.error("Service exception: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);
        } else if (ex instanceof BusinessException) {
            log.warn("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        } else {
            log.error("Application exception: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(ex.getHttpStatusCode()));
    }

    /**
     * Handle validation exceptions from @Valid annotations.
     * Provides detailed field-level validation errors.
     *
     * @param ex the validation exception
     * @return an error response with validation details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, Object> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "ValidationError",
                "Input validation failed",
                "VALIDATION_FAILED",
                validationErrors,
                LocalDateTime.now()
        );

        log.warn("Validation error: {}", validationErrors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Spring Security access denied exceptions.
     *
     * @param ex the access denied exception
     * @return an error response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "AccessDenied",
                "Access denied: " + ex.getMessage(),
                "ACCESS_DENIED",
                LocalDateTime.now()
        );

        log.warn("Access denied: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle response status exceptions.
     *
     * @param ex the response status exception
     * @return an error response
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getStatusCode().value(),
                "ResponseStatusError",
                ex.getReason() != null ? ex.getReason() : "An error occurred",
                "RESPONSE_STATUS_ERROR",
                LocalDateTime.now()
        );

        log.error("Response status exception: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }

    /**
     * Handle illegal argument exceptions.
     *
     * @param ex the illegal argument exception
     * @return an error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "IllegalArgument",
                ex.getMessage(),
                "ILLEGAL_ARGUMENT",
                LocalDateTime.now()
        );

        log.warn("Illegal argument: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle illegal state exceptions.
     *
     * @param ex the illegal state exception
     * @return an error response
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "IllegalState",
                ex.getMessage(),
                "ILLEGAL_STATE",
                LocalDateTime.now()
        );

        log.error("Illegal state: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle all other unhandled exceptions.
     * This serves as a fallback to ensure no exception goes unhandled.
     *
     * @param ex      the exception
     * @param request the web request
     * @return an error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "InternalServerError",
                "An unexpected error occurred",
                "INTERNAL_SERVER_ERROR",
                LocalDateTime.now()
        );

        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

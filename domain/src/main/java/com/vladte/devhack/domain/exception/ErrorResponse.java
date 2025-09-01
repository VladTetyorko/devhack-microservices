package com.vladte.devhack.domain.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Enhanced error response for REST API.
 * Provides a consistent format for all error responses with support for
 * error codes, parameters, and additional details.
 * Follows the Single Responsibility Principle by handling only error response formatting
 * and supports the DRY principle by providing a reusable error response structure.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * HTTP status code
     */
    private int status;

    /**
     * Error type/category
     */
    private String error;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * Machine-readable error code for client handling
     */
    private String errorCode;

    /**
     * Additional error details or validation errors
     */
    private Map<String, Object> details;

    /**
     * Timestamp when the error occurred
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Constructor for basic error response.
     *
     * @param status    HTTP status code
     * @param error     error type
     * @param message   error message
     * @param timestamp when the error occurred
     */
    public ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
    }

    /**
     * Constructor for error response with error code.
     *
     * @param status    HTTP status code
     * @param error     error type
     * @param message   error message
     * @param errorCode machine-readable error code
     * @param timestamp when the error occurred
     */
    public ErrorResponse(int status, String error, String message, String errorCode, LocalDateTime timestamp) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = timestamp;
    }

    /**
     * Creates an ErrorResponse from a BaseException.
     *
     * @param exception the base exception
     * @return the error response
     */
    public static ErrorResponse fromException(BaseException exception) {
        return new ErrorResponse(
                exception.getHttpStatusCode(),
                exception.getClass().getSimpleName().replace("Exception", ""),
                exception.getMessage(),
                exception.getErrorCode(),
                LocalDateTime.now()
        );
    }

    /**
     * Creates an ErrorResponse with additional details.
     *
     * @param exception the base exception
     * @param details   additional error details
     * @return the error response
     */
    public static ErrorResponse fromException(BaseException exception, Map<String, Object> details) {
        ErrorResponse response = fromException(exception);
        response.setDetails(details);
        return response;
    }
}

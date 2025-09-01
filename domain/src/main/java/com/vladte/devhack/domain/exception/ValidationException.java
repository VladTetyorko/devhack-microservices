package com.vladte.devhack.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when input validation fails.
 * This exception represents errors in data validation that occur
 * when user input or data doesn't meet the required criteria.
 * <p>
 * Follows the Single Responsibility Principle by handling only validation errors
 * and the Open/Closed Principle by being extensible for specific validation scenarios.
 */
public class ValidationException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "VALIDATION_ERROR";

    /**
     * Constructs a new validation exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new validation exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new validation exception with error code and parameters.
     *
     * @param message    the detail message
     * @param errorCode  the error code
     * @param parameters the parameters for message formatting
     */
    public ValidationException(String message, String errorCode, Object... parameters) {
        super(message, errorCode, parameters);
    }

    /**
     * Constructs a new validation exception with error code, parameters and cause.
     *
     * @param message    the detail message
     * @param cause      the cause
     * @param errorCode  the error code
     * @param parameters the parameters for message formatting
     */
    public ValidationException(String message, Throwable cause, String errorCode, Object... parameters) {
        super(message, cause, errorCode, parameters);
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }

    @Override
    public int getHttpStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }
}
package com.vladte.devhack.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when AI service operations fail.
 * This exception represents errors that occur during AI-related operations,
 * such as question generation, answer evaluation, or AI API communication failures.
 * <p>
 * Follows the Single Responsibility Principle by handling only AI service errors
 * and extends ServiceException to maintain the exception hierarchy.
 */
public class AiServiceException extends ServiceException {

    private static final String DEFAULT_ERROR_CODE = "AI_SERVICE_ERROR";

    /**
     * Constructs a new AI service exception with the specified detail message.
     *
     * @param message the detail message
     */
    public AiServiceException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    /**
     * Constructs a new AI service exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public AiServiceException(String message, Throwable cause) {
        super(message, cause, DEFAULT_ERROR_CODE);
    }

    /**
     * Constructs a new AI service exception with error code and parameters.
     *
     * @param message    the detail message
     * @param errorCode  the error code
     * @param parameters the parameters for message formatting
     */
    public AiServiceException(String message, String errorCode, Object... parameters) {
        super(message, errorCode, parameters);
    }

    /**
     * Constructs a new AI service exception with error code, parameters and cause.
     *
     * @param message    the detail message
     * @param cause      the cause
     * @param errorCode  the error code
     * @param parameters the parameters for message formatting
     */
    public AiServiceException(String message, Throwable cause, String errorCode, Object... parameters) {
        super(message, cause, errorCode, parameters);
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }

    @Override
    public int getHttpStatusCode() {
        return HttpStatus.SERVICE_UNAVAILABLE.value();
    }
}
package com.vladte.devhack.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when data processing operations fail.
 * This exception represents errors that occur during data processing,
 * such as file parsing, data transformation, or data validation failures.
 * <p>
 * Follows the Single Responsibility Principle by handling only data processing errors
 * and extends ServiceException as these are typically service-level operations.
 */
public class DataProcessingException extends ServiceException {

    private static final String DEFAULT_ERROR_CODE = "DATA_PROCESSING_ERROR";

    /**
     * Constructs a new data processing exception with the specified detail message.
     *
     * @param message the detail message
     */
    public DataProcessingException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    /**
     * Constructs a new data processing exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public DataProcessingException(String message, Throwable cause) {
        super(message, cause, DEFAULT_ERROR_CODE);
    }

    /**
     * Constructs a new data processing exception with error code and parameters.
     *
     * @param message    the detail message
     * @param errorCode  the error code
     * @param parameters the parameters for message formatting
     */
    public DataProcessingException(String message, String errorCode, Object... parameters) {
        super(message, errorCode, parameters);
    }

    /**
     * Constructs a new data processing exception with error code, parameters and cause.
     *
     * @param message    the detail message
     * @param cause      the cause
     * @param errorCode  the error code
     * @param parameters the parameters for message formatting
     */
    public DataProcessingException(String message, Throwable cause, String errorCode, Object... parameters) {
        super(message, cause, errorCode, parameters);
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }

    @Override
    public int getHttpStatusCode() {
        return HttpStatus.UNPROCESSABLE_ENTITY.value();
    }
}
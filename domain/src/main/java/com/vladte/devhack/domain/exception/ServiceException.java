package com.vladte.devhack.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when service layer operations fail.
 * This exception represents errors that occur during service operations,
 * such as external service failures, configuration issues, or internal processing errors.
 * <p>
 * Follows the Single Responsibility Principle by handling only service-related errors
 * and the Open/Closed Principle by being extensible for specific service scenarios.
 */
public class ServiceException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "SERVICE_ERROR";

    /**
     * Constructs a new service exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new service exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new service exception with error code and parameters.
     *
     * @param message    the detail message
     * @param errorCode  the error code
     * @param parameters the parameters for message formatting
     */
    public ServiceException(String message, String errorCode, Object... parameters) {
        super(message, errorCode, parameters);
    }

    /**
     * Constructs a new service exception with error code, parameters and cause.
     *
     * @param message    the detail message
     * @param cause      the cause
     * @param errorCode  the error code
     * @param parameters the parameters for message formatting
     */
    public ServiceException(String message, Throwable cause, String errorCode, Object... parameters) {
        super(message, cause, errorCode, parameters);
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }

    @Override
    public int getHttpStatusCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
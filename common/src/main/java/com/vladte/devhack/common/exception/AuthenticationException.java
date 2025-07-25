package com.vladte.devhack.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication or authorization fails.
 * This exception represents errors that occur during user authentication,
 * authorization checks, or security-related operations.
 * <p>
 * Follows the Single Responsibility Principle by handling only authentication/authorization errors
 * and extends BusinessException as these are typically business rule violations.
 */
public class AuthenticationException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "AUTHENTICATION_ERROR";

    /**
     * Constructs a new authentication exception with the specified detail message.
     *
     * @param message the detail message
     */
    public AuthenticationException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    /**
     * Constructs a new authentication exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause, DEFAULT_ERROR_CODE);
    }

    /**
     * Constructs a new authentication exception with error code and parameters.
     *
     * @param message    the detail message
     * @param errorCode  the error code
     * @param parameters the parameters for message formatting
     */
    public AuthenticationException(String message, String errorCode, Object... parameters) {
        super(message, errorCode, parameters);
    }

    /**
     * Constructs a new authentication exception with error code, parameters and cause.
     *
     * @param message    the detail message
     * @param cause      the cause
     * @param errorCode  the error code
     * @param parameters the parameters for message formatting
     */
    public AuthenticationException(String message, Throwable cause, String errorCode, Object... parameters) {
        super(message, cause, errorCode, parameters);
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }

    @Override
    public int getHttpStatusCode() {
        return HttpStatus.UNAUTHORIZED.value();
    }
}
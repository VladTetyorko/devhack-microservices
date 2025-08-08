package com.vladte.devhack.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when business logic rules are violated.
 * This exception represents errors in business operations that are not related
 * to technical issues but rather to business rule violations.
 * <p>
 * Follows the Open/Closed Principle by being open for extension
 * and the Single Responsibility Principle by handling only business logic errors.
 */
public class BusinessException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "BUSINESS_ERROR";

    /**
     * Constructs a new business exception with the specified detail message.
     *
     * @param message the detail message
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Constructs a new business exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new business exception with error code and parameters.
     *
     * @param message    the detail message
     * @param errorCode  the error code
     * @param parameters the parameters for message formatting
     */
    public BusinessException(String message, String errorCode, Object... parameters) {
        super(message, errorCode, parameters);
    }

    /**
     * Constructs a new business exception with error code, parameters and cause.
     *
     * @param message    the detail message
     * @param cause      the cause
     * @param errorCode  the error code
     * @param parameters the parameters for message formatting
     */
    public BusinessException(String message, Throwable cause, String errorCode, Object... parameters) {
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
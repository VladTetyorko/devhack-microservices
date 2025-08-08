package com.vladte.devhack.common.exception;

/**
 * Base exception class for all custom exceptions in the DevHack application.
 * Follows the Single Responsibility Principle by providing common exception functionality.
 * <p>
 * This class serves as the foundation for all business exceptions and provides
 * consistent error handling across the application.
 */
public abstract class BaseException extends RuntimeException {

    private final String errorCode;
    private final Object[] parameters;

    /**
     * Constructs a new base exception with the specified detail message.
     *
     * @param message the detail message
     */
    protected BaseException(String message) {
        super(message);
        this.errorCode = getDefaultErrorCode();
        this.parameters = new Object[0];
    }

    /**
     * Constructs a new base exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    protected BaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = getDefaultErrorCode();
        this.parameters = new Object[0];
    }

    /**
     * Constructs a new base exception with error code and parameters.
     *
     * @param message    the detail message
     * @param errorCode  the error code
     * @param parameters the parameters for message formatting
     */
    protected BaseException(String message, String errorCode, Object... parameters) {
        super(message);
        this.errorCode = errorCode;
        this.parameters = parameters != null ? parameters.clone() : new Object[0];
    }

    /**
     * Constructs a new base exception with error code, parameters and cause.
     *
     * @param message    the detail message
     * @param cause      the cause
     * @param errorCode  the error code
     * @param parameters the parameters for message formatting
     */
    protected BaseException(String message, Throwable cause, String errorCode, Object... parameters) {
        super(message, cause);
        this.errorCode = errorCode;
        this.parameters = parameters != null ? parameters.clone() : new Object[0];
    }

    /**
     * Gets the error code for this exception.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the parameters for this exception.
     *
     * @return the parameters array
     */
    public Object[] getParameters() {
        return parameters.clone();
    }

    /**
     * Gets the default error code for this exception type.
     * Subclasses should override this method to provide their specific error code.
     *
     * @return the default error code
     */
    protected abstract String getDefaultErrorCode();

    /**
     * Gets the HTTP status code that should be returned for this exception.
     * Subclasses can override this method to specify different status codes.
     *
     * @return the HTTP status code
     */
    public abstract int getHttpStatusCode();
}
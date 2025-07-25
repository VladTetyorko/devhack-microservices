package com.vladte.devhack.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an entity is not found.
 * Used to indicate that a requested resource does not exist.
 * Now extends BaseException to integrate with the comprehensive exception handling system
 * while maintaining backward compatibility with existing code.
 */
public class EntityNotFoundException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "ENTITY_NOT_FOUND";

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public EntityNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception for a specific entity type and ID.
     *
     * @param entityType the entity type
     * @param id         the entity ID
     */
    public EntityNotFoundException(String entityType, Object id) {
        super(entityType + " not found with id: " + id, DEFAULT_ERROR_CODE, entityType, id);
    }

    /**
     * Constructs a new exception with error code and parameters.
     *
     * @param message    the detail message
     * @param errorCode  the error code
     * @param parameters the parameters for message formatting
     */
    public EntityNotFoundException(String message, String errorCode, Object... parameters) {
        super(message, errorCode, parameters);
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }

    @Override
    public int getHttpStatusCode() {
        return HttpStatus.NOT_FOUND.value();
    }
}

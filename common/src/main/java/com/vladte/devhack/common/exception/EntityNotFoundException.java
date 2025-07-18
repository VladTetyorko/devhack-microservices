package com.vladte.devhack.common.exception;

/**
 * Exception thrown when an entity is not found.
 * Used to indicate that a requested resource does not exist.
 */
public class EntityNotFoundException extends RuntimeException {

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
        super(entityType + " not found with id: " + id);
    }
}
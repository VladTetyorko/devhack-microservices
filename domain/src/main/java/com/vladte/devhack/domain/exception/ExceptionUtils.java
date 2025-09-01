package com.vladte.devhack.domain.exception;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Utility class for common exception handling patterns.
 * Provides reusable methods that follow DRY principles and reduce code duplication
 * across services.
 * <p>
 * Follows the Single Responsibility Principle by handling only exception-related utilities
 * and supports the DRY principle by providing reusable exception handling patterns.
 */
public final class ExceptionUtils {

    private ExceptionUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Throws EntityNotFoundException if the optional is empty.
     *
     * @param optional   the optional to check
     * @param entityType the entity type name
     * @param id         the entity ID
     * @param <T>        the entity type
     * @return the entity if present
     * @throws EntityNotFoundException if the optional is empty
     */
    public static <T> T requirePresent(Optional<T> optional, String entityType, Object id) {
        return optional.orElseThrow(() -> new EntityNotFoundException(entityType, id));
    }

    /**
     * Throws EntityNotFoundException if the optional is empty with a custom message.
     *
     * @param optional the optional to check
     * @param message  the custom error message
     * @param <T>      the entity type
     * @return the entity if present
     * @throws EntityNotFoundException if the optional is empty
     */
    public static <T> T requirePresent(Optional<T> optional, String message) {
        return optional.orElseThrow(() -> new EntityNotFoundException(message));
    }

    /**
     * Validates that a parameter is not null, throwing ValidationException if it is.
     *
     * @param parameter     the parameter to check
     * @param parameterName the parameter name for error message
     * @param <T>           the parameter type
     * @return the parameter if not null
     * @throws ValidationException if the parameter is null
     */
    public static <T> T requireNonNull(T parameter, String parameterName) {
        if (parameter == null) {
            throw new ValidationException(parameterName + " cannot be null", "NULL_PARAMETER", parameterName);
        }
        return parameter;
    }

    /**
     * Validates that a string parameter is not null or empty.
     *
     * @param parameter     the string parameter to check
     * @param parameterName the parameter name for error message
     * @return the parameter if not null or empty
     * @throws ValidationException if the parameter is null or empty
     */
    public static String requireNonEmpty(String parameter, String parameterName) {
        if (parameter == null || parameter.trim().isEmpty()) {
            throw new ValidationException(parameterName + " cannot be null or empty", "EMPTY_PARAMETER", parameterName);
        }
        return parameter;
    }

    /**
     * Validates a business rule condition, throwing BusinessException if false.
     *
     * @param condition the condition to check
     * @param message   the error message if condition is false
     * @throws BusinessException if the condition is false
     */
    public static void requireBusinessRule(boolean condition, String message) {
        if (!condition) {
            throw new BusinessException(message);
        }
    }

    /**
     * Validates a business rule condition with error code, throwing BusinessException if false.
     *
     * @param condition  the condition to check
     * @param message    the error message if condition is false
     * @param errorCode  the error code
     * @param parameters the parameters for message formatting
     * @throws BusinessException if the condition is false
     */
    public static void requireBusinessRule(boolean condition, String message, String errorCode, Object... parameters) {
        if (!condition) {
            throw new BusinessException(message, errorCode, parameters);
        }
    }

    /**
     * Executes a supplier and wraps any checked exception in a ServiceException.
     *
     * @param supplier the supplier to execute
     * @param message  the error message if an exception occurs
     * @param <T>      the return type
     * @return the result of the supplier
     * @throws ServiceException if any exception occurs during execution
     */
    public static <T> T executeWithServiceException(Supplier<T> supplier, String message) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new ServiceException(message, e);
        }
    }

    /**
     * Executes a runnable and wraps any checked exception in a ServiceException.
     *
     * @param runnable the runnable to execute
     * @param message  the error message if an exception occurs
     * @throws ServiceException if any exception occurs during execution
     */
    public static void executeWithServiceException(Runnable runnable, String message) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new ServiceException(message, e);
        }
    }

    /**
     * Validates that a numeric parameter is positive.
     *
     * @param number        the number to check
     * @param parameterName the parameter name for error message
     * @return the number if positive
     * @throws ValidationException if the number is not positive
     */
    public static int requirePositive(int number, String parameterName) {
        if (number <= 0) {
            throw new ValidationException(parameterName + " must be positive", "INVALID_NUMBER", parameterName, number);
        }
        return number;
    }

    /**
     * Validates that a numeric parameter is non-negative.
     *
     * @param number        the number to check
     * @param parameterName the parameter name for error message
     * @return the number if non-negative
     * @throws ValidationException if the number is negative
     */
    public static int requireNonNegative(int number, String parameterName) {
        if (number < 0) {
            throw new ValidationException(parameterName + " cannot be negative", "INVALID_NUMBER", parameterName, number);
        }
        return number;
    }
}
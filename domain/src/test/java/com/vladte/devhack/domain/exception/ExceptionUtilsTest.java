package com.vladte.devhack.domain.exception;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ExceptionUtils utility methods.
 * Verifies that exception handling utilities work correctly and follow expected patterns.
 */
class ExceptionUtilsTest {

    @Test
    void requirePresent_WithPresentOptional_ShouldReturnValue() {
        // Given
        String expectedValue = "test-value";
        Optional<String> optional = Optional.of(expectedValue);

        // When
        String result = ExceptionUtils.requirePresent(optional, "TestEntity", "123");

        // Then
        assertEquals(expectedValue, result);
    }

    @Test
    void requirePresent_WithEmptyOptional_ShouldThrowEntityNotFoundException() {
        // Given
        Optional<String> optional = Optional.empty();

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> ExceptionUtils.requirePresent(optional, "TestEntity", "123")
        );

        assertEquals("TestEntity not found with id: 123", exception.getMessage());
        assertEquals("ENTITY_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void requirePresent_WithCustomMessage_ShouldThrowEntityNotFoundExceptionWithCustomMessage() {
        // Given
        Optional<String> optional = Optional.empty();
        String customMessage = "Custom entity not found message";

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> ExceptionUtils.requirePresent(optional, customMessage)
        );

        assertEquals(customMessage, exception.getMessage());
    }

    @Test
    void requireNonNull_WithNonNullParameter_ShouldReturnParameter() {
        // Given
        String parameter = "test-parameter";

        // When
        String result = ExceptionUtils.requireNonNull(parameter, "testParam");

        // Then
        assertEquals(parameter, result);
    }

    @Test
    void requireNonNull_WithNullParameter_ShouldThrowValidationException() {
        // Given
        String parameter = null;

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ExceptionUtils.requireNonNull(parameter, "testParam")
        );

        assertEquals("testParam cannot be null", exception.getMessage());
        assertEquals("NULL_PARAMETER", exception.getErrorCode());
    }

    @Test
    void requireNonEmpty_WithNonEmptyString_ShouldReturnString() {
        // Given
        String parameter = "test-string";

        // When
        String result = ExceptionUtils.requireNonEmpty(parameter, "testParam");

        // Then
        assertEquals(parameter, result);
    }

    @Test
    void requireNonEmpty_WithNullString_ShouldThrowValidationException() {
        // Given
        String parameter = null;

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ExceptionUtils.requireNonEmpty(parameter, "testParam")
        );

        assertEquals("testParam cannot be null or empty", exception.getMessage());
        assertEquals("EMPTY_PARAMETER", exception.getErrorCode());
    }

    @Test
    void requireNonEmpty_WithEmptyString_ShouldThrowValidationException() {
        // Given
        String parameter = "   ";

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ExceptionUtils.requireNonEmpty(parameter, "testParam")
        );

        assertEquals("testParam cannot be null or empty", exception.getMessage());
        assertEquals("EMPTY_PARAMETER", exception.getErrorCode());
    }

    @Test
    void requireBusinessRule_WithTrueCondition_ShouldNotThrowException() {
        // Given
        boolean condition = true;

        // When & Then
        assertDoesNotThrow(() -> ExceptionUtils.requireBusinessRule(condition, "Business rule violated"));
    }

    @Test
    void requireBusinessRule_WithFalseCondition_ShouldThrowBusinessException() {
        // Given
        boolean condition = false;
        String message = "Business rule violated";

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> ExceptionUtils.requireBusinessRule(condition, message)
        );

        assertEquals(message, exception.getMessage());
    }

    @Test
    void requireBusinessRule_WithFalseConditionAndErrorCode_ShouldThrowBusinessExceptionWithErrorCode() {
        // Given
        boolean condition = false;
        String message = "Business rule violated";
        String errorCode = "CUSTOM_BUSINESS_ERROR";

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> ExceptionUtils.requireBusinessRule(condition, message, errorCode, "param1")
        );

        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
    }

    @Test
    void requirePositive_WithPositiveNumber_ShouldReturnNumber() {
        // Given
        int number = 5;

        // When
        int result = ExceptionUtils.requirePositive(number, "testNumber");

        // Then
        assertEquals(number, result);
    }

    @Test
    void requirePositive_WithZero_ShouldThrowValidationException() {
        // Given
        int number = 0;

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ExceptionUtils.requirePositive(number, "testNumber")
        );

        assertEquals("testNumber must be positive", exception.getMessage());
        assertEquals("INVALID_NUMBER", exception.getErrorCode());
    }

    @Test
    void requirePositive_WithNegativeNumber_ShouldThrowValidationException() {
        // Given
        int number = -1;

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ExceptionUtils.requirePositive(number, "testNumber")
        );

        assertEquals("testNumber must be positive", exception.getMessage());
        assertEquals("INVALID_NUMBER", exception.getErrorCode());
    }

    @Test
    void requireNonNegative_WithPositiveNumber_ShouldReturnNumber() {
        // Given
        int number = 5;

        // When
        int result = ExceptionUtils.requireNonNegative(number, "testNumber");

        // Then
        assertEquals(number, result);
    }

    @Test
    void requireNonNegative_WithZero_ShouldReturnZero() {
        // Given
        int number = 0;

        // When
        int result = ExceptionUtils.requireNonNegative(number, "testNumber");

        // Then
        assertEquals(0, result);
    }

    @Test
    void requireNonNegative_WithNegativeNumber_ShouldThrowValidationException() {
        // Given
        int number = -1;

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ExceptionUtils.requireNonNegative(number, "testNumber")
        );

        assertEquals("testNumber cannot be negative", exception.getMessage());
        assertEquals("INVALID_NUMBER", exception.getErrorCode());
    }

    @Test
    void executeWithServiceException_WithSuccessfulSupplier_ShouldReturnResult() {
        // Given
        String expectedResult = "success";

        // When
        String result = ExceptionUtils.executeWithServiceException(() -> expectedResult, "Operation failed");

        // Then
        assertEquals(expectedResult, result);
    }

    @Test
    void executeWithServiceException_WithFailingSupplier_ShouldThrowServiceException() {
        // Given
        RuntimeException originalException = new RuntimeException("Original error");

        // When & Then
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> ExceptionUtils.executeWithServiceException(() -> {
                    throw originalException;
                }, "Operation failed")
        );

        assertEquals("Operation failed", exception.getMessage());
        assertEquals(originalException, exception.getCause());
    }
}
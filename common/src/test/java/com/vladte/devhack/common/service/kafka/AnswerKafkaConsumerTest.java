package com.vladte.devhack.common.service.kafka;

import com.vladte.devhack.common.service.BaseServiceTest;
import com.vladte.devhack.infra.model.KafkaMessage;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the AnswerKafkaConsumer.
 */
@DisplayName("Answer Kafka Consumer Tests")
public class AnswerKafkaConsumerTest extends BaseServiceTest {

    private AnswerKafkaConsumer answerKafkaConsumer;

    @BeforeEach
    public void setup() {
        answerKafkaConsumer = new AnswerKafkaConsumer();
    }

    @Test
    @DisplayName("Register pending request should return a CompletableFuture")
    @Description("Test that registering a pending request returns a non-completed CompletableFuture")
    @Severity(SeverityLevel.CRITICAL)
    public void testRegisterPendingRequest() {
        // Arrange
        String messageId = UUID.randomUUID().toString();

        // Act
        CompletableFuture<Map<String, Object>> future = answerKafkaConsumer.registerPendingRequest(messageId);

        // Assert
        assertNotNull(future);
        assertFalse(future.isDone());
    }

    @Test
    @DisplayName("Consume answer feedback result should complete future with parsed result")
    @Description("Test that consuming an answer feedback result completes the future with correctly parsed score and feedback")
    @Severity(SeverityLevel.CRITICAL)
    public void testConsumeAnswerFeedbackResult() throws ExecutionException, InterruptedException, TimeoutException {
        // Arrange
        String messageId = UUID.randomUUID().toString();
        CompletableFuture<Map<String, Object>> future = answerKafkaConsumer.registerPendingRequest(messageId);

        KafkaMessage message = new KafkaMessage(
                messageId,
                "ai-app",
                "main-app",
                "ai-response",
                "score:85||feedback:Good answer, but could be improved",
                LocalDateTime.now()
        );

        // Act
        answerKafkaConsumer.consumeAnswerFeedbackResult(message);

        // Assert
        assertTrue(future.isDone());
        Map<String, Object> result = future.get(1, TimeUnit.SECONDS);
        assertEquals(85.0, result.get("score"));
        assertEquals("Good answer, but could be improved", result.get("feedback"));
    }

    @Test
    @DisplayName("Consume answer feedback result with error should complete future exceptionally")
    @Description("Test that consuming an answer feedback result with an error message completes the future exceptionally")
    @Severity(SeverityLevel.CRITICAL)
    public void testConsumeAnswerFeedbackResultWithError() {
        // Arrange
        String messageId = UUID.randomUUID().toString();
        CompletableFuture<Map<String, Object>> future = answerKafkaConsumer.registerPendingRequest(messageId);

        KafkaMessage message = new KafkaMessage(
                messageId,
                "ai-app",
                "main-app",
                "ai-response",
                "Error: Failed to process answer",
                LocalDateTime.now()
        );

        // Act
        answerKafkaConsumer.consumeAnswerFeedbackResult(message);

        // Assert
        assertTrue(future.isCompletedExceptionally());
        assertThrows(ExecutionException.class, () -> future.get(1, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Consume answer feedback result with boolean payload should parse as cheating check")
    @Description("Test that consuming an answer feedback result with a boolean payload correctly parses it as a cheating check")
    @Severity(SeverityLevel.CRITICAL)
    public void testConsumeAnswerFeedbackResultWithBooleanPayload() throws ExecutionException, InterruptedException, TimeoutException {
        // Arrange
        String messageId = UUID.randomUUID().toString();
        CompletableFuture<Map<String, Object>> future = answerKafkaConsumer.registerPendingRequest(messageId);

        KafkaMessage message = new KafkaMessage(
                messageId,
                "ai-app",
                "main-app",
                "ai-response",
                "true",
                LocalDateTime.now()
        );

        // Act
        answerKafkaConsumer.consumeAnswerFeedbackResult(message);

        // Assert
        assertTrue(future.isDone());
        Map<String, Object> result = future.get(1, TimeUnit.SECONDS);
        assertTrue((Boolean) result.get("isCheating"));
    }

    @Test
    @DisplayName("Consume answer feedback result with wrong message type should not complete future")
    @Description("Test that consuming an answer feedback result with the wrong message type does not complete the future")
    @Severity(SeverityLevel.NORMAL)
    public void testConsumeAnswerFeedbackResultWithWrongType() throws InterruptedException {
        // Arrange
        String messageId = UUID.randomUUID().toString();
        CompletableFuture<Map<String, Object>> future = answerKafkaConsumer.registerPendingRequest(messageId);

        KafkaMessage message = new KafkaMessage(
                messageId,
                "ai-app",
                "main-app",
                "wrong-type",
                "score:85||feedback:Good answer, but could be improved",
                LocalDateTime.now()
        );

        // Act
        answerKafkaConsumer.consumeAnswerFeedbackResult(message);

        // Assert
        assertFalse(future.isDone());
    }

    @Test
    @DisplayName("Consume answer feedback result with unknown message ID should not throw exception")
    @Description("Test that consuming an answer feedback result with an unknown message ID does not throw an exception")
    @Severity(SeverityLevel.NORMAL)
    public void testConsumeAnswerFeedbackResultWithUnknownId() {
        // Arrange
        String messageId = UUID.randomUUID().toString();

        KafkaMessage message = new KafkaMessage(
                messageId,
                "ai-app",
                "main-app",
                "ai-response",
                "score:85||feedback:Good answer, but could be improved",
                LocalDateTime.now()
        );

        // Act & Assert
        assertDoesNotThrow(() -> answerKafkaConsumer.consumeAnswerFeedbackResult(message));
    }
}

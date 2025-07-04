package com.vladte.devhack.common.service.kafka;

import com.vladte.devhack.common.model.QuestionGenerationResponse;
import com.vladte.devhack.common.service.BaseServiceTest;
import com.vladte.devhack.common.service.generations.QuestionParsingService;
import com.vladte.devhack.common.service.kafka.concumers.QuestionKafkaConsumer;
import com.vladte.devhack.infra.model.KafkaMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the QuestionKafkaConsumer.
 */
public class QuestionKafkaConsumerTest extends BaseServiceTest {

    @Mock
    private QuestionParsingService questionParsingService;

    private QuestionKafkaConsumer questionKafkaConsumer;

    @BeforeEach
    public void setup() {
        questionKafkaConsumer = new QuestionKafkaConsumer(questionParsingService);
    }

    @Test
    @DisplayName("Register pending request should return a CompletableFuture")
    public void testRegisterPendingRequest() {
        // Arrange
        String messageId = UUID.randomUUID().toString();

        // Act
        CompletableFuture<QuestionGenerationResponse> future = questionKafkaConsumer.registerPendingRequest(messageId);

        // Assert
        assertNotNull(future);
        assertFalse(future.isDone());
    }

    @Test
    @DisplayName("Consume from AI should complete future with parsed questions")
    public void testConsumeFromAi() throws ExecutionException, InterruptedException, TimeoutException {
        // Arrange
        String messageId = UUID.randomUUID().toString();
        CompletableFuture<QuestionGenerationResponse> future = questionKafkaConsumer.registerPendingRequest(messageId);

        String payload = "Question: What is Java?\nJava is a programming language.\nQuestion: What is Spring?";
        List<String> parsedQuestions = Arrays.asList("What is Java?", "What is Spring?");

        when(questionParsingService.parseQuestionTexts(payload)).thenReturn(parsedQuestions);

        KafkaMessage message = new KafkaMessage(
                messageId,
                "ai-app",
                "main-app",
                "ai-response",
                payload,
                LocalDateTime.now()
        );

        // Act
        questionKafkaConsumer.consumeFromAi(message);

        // Assert
        assertTrue(future.isDone());
        QuestionGenerationResponse response = future.get(1, TimeUnit.SECONDS);
        assertEquals("success", response.getStatus());
        assertEquals(parsedQuestions, response.getQuestionTexts());
    }

    @Test
    @DisplayName("Consume from AI with error should complete future with error response")
    public void testConsumeFromAiWithError() throws ExecutionException, InterruptedException, TimeoutException {
        // Arrange
        String messageId = UUID.randomUUID().toString();
        CompletableFuture<QuestionGenerationResponse> future = questionKafkaConsumer.registerPendingRequest(messageId);

        KafkaMessage message = new KafkaMessage(
                messageId,
                "ai-app",
                "main-app",
                "ai-response",
                "Error: Failed to generate questions",
                LocalDateTime.now()
        );

        // Act
        questionKafkaConsumer.consumeFromAi(message);

        // Assert
        assertTrue(future.isDone());
        QuestionGenerationResponse response = future.get(1, TimeUnit.SECONDS);
        assertEquals("error", response.getStatus());
        assertEquals("Error: Failed to generate questions", response.getErrorMessage());
    }

    @Test
    @DisplayName("Consume from AI with parsing exception should complete future with error response")
    public void testConsumeFromAiWithParsingException() throws ExecutionException, InterruptedException, TimeoutException {
        // Arrange
        String messageId = UUID.randomUUID().toString();
        CompletableFuture<QuestionGenerationResponse> future = questionKafkaConsumer.registerPendingRequest(messageId);

        String payload = "Invalid format";
        when(questionParsingService.parseQuestionTexts(anyString())).thenThrow(new RuntimeException("Parsing error"));

        KafkaMessage message = new KafkaMessage(
                messageId,
                "ai-app",
                "main-app",
                "ai-response",
                payload,
                LocalDateTime.now()
        );

        // Act
        questionKafkaConsumer.consumeFromAi(message);

        // Assert
        assertTrue(future.isDone());
        QuestionGenerationResponse response = future.get(1, TimeUnit.SECONDS);
        assertEquals("error", response.getStatus());
        assertTrue(response.getErrorMessage().contains("Parsing error"));
    }

    @Test
    @DisplayName("Consume from AI with wrong message type should not complete future")
    public void testConsumeFromAiWithWrongType() throws InterruptedException {
        // Arrange
        String messageId = UUID.randomUUID().toString();
        CompletableFuture<QuestionGenerationResponse> future = questionKafkaConsumer.registerPendingRequest(messageId);

        KafkaMessage message = new KafkaMessage(
                messageId,
                "ai-app",
                "main-app",
                "wrong-type",
                "Question: What is Java?",
                LocalDateTime.now()
        );

        // Act
        questionKafkaConsumer.consumeFromAi(message);

        // Assert
        assertFalse(future.isDone());
    }

    @Test
    @DisplayName("Consume from AI with unknown message ID should not throw exception")
    public void testConsumeFromAiWithUnknownId() {
        // Arrange
        String messageId = UUID.randomUUID().toString();

        KafkaMessage message = new KafkaMessage(
                messageId,
                "ai-app",
                "main-app",
                "ai-response",
                "Question: What is Java?",
                LocalDateTime.now()
        );

        // Act & Assert
        assertDoesNotThrow(() -> questionKafkaConsumer.consumeFromAi(message));
    }
}
package com.vladte.devhack.common.service.kafka;

import com.vladte.devhack.common.config.KafkaTestConfig;
import com.vladte.devhack.common.model.QuestionGenerationResponse;
import com.vladte.devhack.common.service.generations.QuestionParsingService;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.topics.Topics;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Integration tests for Kafka components.
 * Uses an embedded Kafka broker to test the interaction between producers and consumers.
 */
@SpringBootTest
@Import(KafkaTestConfig.class)
@EmbeddedKafka(partitions = 1, topics = {
        Topics.QUESTION_GENERATE_REQUEST,
        Topics.QUESTION_GENERATE_RESULT,
        Topics.ANSWER_FEEDBACK_REQUEST,
        Topics.ANSWER_FEEDBACK_RESULT
})
@DirtiesContext
@ActiveProfiles("test")
@DisplayName("Kafka Integration Tests")
public class KafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    @Autowired
    private QuestionParsingService questionParsingService;

    private AnswerKafkaConsumer answerKafkaConsumer;
    private QuestionKafkaConsumer questionKafkaConsumer;
    private KafkaProducerService kafkaProducerService;
    private AnswerKafkaProvider answerKafkaProvider;
    private QuestionKafkaProvider questionKafkaProvider;

    @BeforeEach
    public void setup() {
        answerKafkaConsumer = new AnswerKafkaConsumer();
        questionKafkaConsumer = new QuestionKafkaConsumer(questionParsingService);
        kafkaProducerService = new KafkaProducerService(kafkaTemplate);
        answerKafkaProvider = new AnswerKafkaProvider(kafkaProducerService);
        questionKafkaProvider = new QuestionKafkaProvider(kafkaProducerService);
    }

    @Test
    @DisplayName("Answer feedback request and response should work end-to-end")
    @Description("Test that the answer feedback request and response flow works correctly from end to end")
    @Severity(SeverityLevel.CRITICAL)
    public void testAnswerFeedbackEndToEnd() throws ExecutionException, InterruptedException, TimeoutException {
        // Arrange
        String messageId = UUID.randomUUID().toString();
        String questionText = "What is the capital of France?";
        String answerText = "The capital of France is Paris.";

        // Register a pending request with the consumer
        CompletableFuture<Map<String, Object>> responseFuture = answerKafkaConsumer.registerPendingRequest(messageId);

        // Create a response message that will be sent back
        KafkaMessage responseMessage = new KafkaMessage(
                messageId,
                "ai-app",
                "main-app",
                "ai-response",
                "score:85||feedback:Good answer, but could be improved",
                LocalDateTime.now()
        );

        // Act
        // Send the request
        answerKafkaProvider.sendAnswerFeedbackRequest(messageId, questionText, answerText);

        // Simulate the AI service receiving the request and sending a response
        // In a real scenario, the AI service would process the request and send a response
        kafkaTemplate.send(Topics.ANSWER_FEEDBACK_RESULT, messageId, responseMessage);

        // Process the response
        answerKafkaConsumer.consumeAnswerFeedbackResult(responseMessage);

        // Assert
        assertTrue(responseFuture.isDone());
        Map<String, Object> result = responseFuture.get(5, TimeUnit.SECONDS);
        assertEquals(85.0, result.get("score"));
        assertEquals("Good answer, but could be improved", result.get("feedback"));
    }

    @Test
    @DisplayName("Question generation request and response should work end-to-end")
    @Description("Test that the question generation request and response flow works correctly from end to end")
    @Severity(SeverityLevel.CRITICAL)
    public void testQuestionGenerationEndToEnd() throws ExecutionException, InterruptedException, TimeoutException {
        // Arrange
        String messageId = UUID.randomUUID().toString();
        String tagName = "java";
        int count = 2;
        String difficulty = "Medium";

        // Mock the question parsing service
        String responsePayload = "Question: What is Java?\nJava is a programming language.\nQuestion: What is Spring?";
        when(questionParsingService.parseQuestionTexts(responsePayload))
                .thenReturn(Arrays.asList("What is Java?", "What is Spring?"));

        // Register a pending request with the consumer
        CompletableFuture<QuestionGenerationResponse> responseFuture = questionKafkaConsumer.registerPendingRequest(messageId);

        // Create a response message that will be sent back
        KafkaMessage responseMessage = new KafkaMessage(
                messageId,
                "ai-app",
                "main-app",
                "ai-response",
                responsePayload,
                LocalDateTime.now()
        );

        // Act
        // Send the request
        questionKafkaProvider.sendGenerateQuestionsRequest(messageId, tagName, count, difficulty);

        // Simulate the AI service receiving the request and sending a response
        // In a real scenario, the AI service would process the request and send a response
        kafkaTemplate.send(Topics.QUESTION_GENERATE_RESULT, messageId, responseMessage);

        // Process the response
        questionKafkaConsumer.consumeFromAi(responseMessage);

        // Assert
        assertTrue(responseFuture.isDone());
        QuestionGenerationResponse result = responseFuture.get(5, TimeUnit.SECONDS);
        assertEquals("success", result.getStatus());
        assertEquals(2, result.getQuestionTexts().size());
        assertEquals("What is Java?", result.getQuestionTexts().get(0));
        assertEquals("What is Spring?", result.getQuestionTexts().get(1));
    }
}

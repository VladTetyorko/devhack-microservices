package com.vladte.devhack.common.service.kafka;

import com.vladte.devhack.common.service.BaseServiceTest;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.topics.Topics;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the KafkaProducerService.
 */
@DisplayName("Kafka Producer Service Tests")
public class KafkaProducerServiceTest extends BaseServiceTest {

    @Mock
    private KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    public void setup() {
        kafkaProducerService = new KafkaProducerService(kafkaTemplate);
    }

    @Test
    @DisplayName("Send question generate request should call Kafka template with correct topic and message")
    @Description("Test that sending a question generate request calls the Kafka template with the correct topic and message")
    @Severity(SeverityLevel.CRITICAL)
    public void testSendQuestionGenerateRequest() {
        // Arrange
        KafkaMessage message = createTestMessage("question-generate");
        CompletableFuture<SendResult<String, KafkaMessage>> future = new CompletableFuture<>();

        when(kafkaTemplate.send(eq(Topics.QUESTION_GENERATE_REQUEST), eq(message.getId()), eq(message)))
                .thenReturn(future);

        // Act
        CompletableFuture<SendResult<String, KafkaMessage>> result = kafkaProducerService.sendMessage(message);

        // Assert
        assertNotNull(result);
        verify(kafkaTemplate).send(Topics.QUESTION_GENERATE_REQUEST, message.getId(), message);
    }

    @Test
    @DisplayName("Send answer feedback request should call Kafka template with correct topic and message")
    @Description("Test that sending an answer feedback request calls the Kafka template with the correct topic and message")
    @Severity(SeverityLevel.CRITICAL)
    public void testSendAnswerFeedbackRequest() {
        // Arrange
        KafkaMessage message = createTestMessage("answer-feedback");
        CompletableFuture<SendResult<String, KafkaMessage>> future = new CompletableFuture<>();

        when(kafkaTemplate.send(eq(Topics.ANSWER_FEEDBACK_REQUEST), eq(message.getId()), eq(message)))
                .thenReturn(future);

        // Act
        CompletableFuture<SendResult<String, KafkaMessage>> result = kafkaProducerService.sendMessage(message);

        // Assert
        assertNotNull(result);
        verify(kafkaTemplate).send(Topics.ANSWER_FEEDBACK_REQUEST, message.getId(), message);
    }

    /**
     * Creates a test KafkaMessage with the specified type.
     */
    private KafkaMessage createTestMessage(String type) {
        return new KafkaMessage(
                UUID.randomUUID().toString(),
                "test-source",
                "test-destination",
                type,
                "test-payload",
                LocalDateTime.now()
        );
    }
}

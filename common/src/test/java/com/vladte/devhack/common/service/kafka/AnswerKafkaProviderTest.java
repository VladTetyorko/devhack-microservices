package com.vladte.devhack.common.service.kafka;

import com.vladte.devhack.common.service.BaseServiceTest;
import com.vladte.devhack.infra.model.KafkaMessage;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the AnswerKafkaProvider.
 */
@DisplayName("Answer Kafka Provider Tests")
public class AnswerKafkaProviderTest extends BaseServiceTest {

    @Mock
    private KafkaProducerService kafkaProducerService;

    @Captor
    private ArgumentCaptor<KafkaMessage> messageCaptor;

    private AnswerKafkaProvider answerKafkaProvider;

    @BeforeEach
    public void setup() {
        answerKafkaProvider = new AnswerKafkaProvider(kafkaProducerService);
    }

    @Test
    @DisplayName("Send answer feedback request should create correct message and call producer service")
    @Description("Test that sending an answer feedback request creates the correct message and calls the producer service")
    @Severity(SeverityLevel.CRITICAL)
    public void testSendAnswerFeedbackRequest() {
        // Arrange
        String messageId = "test-message-id";
        String questionText = "What is the capital of France?";
        String answerText = "The capital of France is Paris.";

        CompletableFuture<SendResult<String, KafkaMessage>> future = new CompletableFuture<>();
        when(kafkaProducerService.sendAnswerFeedbackRequest(any(KafkaMessage.class))).thenReturn(future);

        // Act
        CompletableFuture<SendResult<String, KafkaMessage>> result =
                answerKafkaProvider.sendAnswerFeedbackRequest(messageId, questionText, answerText);

        // Assert
        assertNotNull(result);
        verify(kafkaProducerService).sendAnswerFeedbackRequest(messageCaptor.capture());

        KafkaMessage capturedMessage = messageCaptor.getValue();
        assertEquals(messageId, capturedMessage.getId());
        assertEquals("main-app", capturedMessage.getSource());
        assertEquals("ai-app", capturedMessage.getDestination());
        assertEquals("check-answer-with-feedback", capturedMessage.getType());
        assertEquals(questionText + "||" + answerText, capturedMessage.getPayload());
    }

    @Test
    @DisplayName("Send answer cheating check request should create correct message and call producer service")
    @Description("Test that sending an answer cheating check request creates the correct message and calls the producer service")
    @Severity(SeverityLevel.CRITICAL)
    public void testSendAnswerCheatingCheckRequest() {
        // Arrange
        String messageId = "test-message-id";
        String questionText = "What is the capital of France?";
        String answerText = "The capital of France is Paris.";

        CompletableFuture<SendResult<String, KafkaMessage>> future = new CompletableFuture<>();
        when(kafkaProducerService.sendAnswerFeedbackRequest(any(KafkaMessage.class))).thenReturn(future);

        // Act
        CompletableFuture<SendResult<String, KafkaMessage>> result =
                answerKafkaProvider.sendAnswerCheatingCheckRequest(messageId, questionText, answerText);

        // Assert
        assertNotNull(result);
        verify(kafkaProducerService).sendAnswerFeedbackRequest(messageCaptor.capture());

        KafkaMessage capturedMessage = messageCaptor.getValue();
        assertEquals(messageId, capturedMessage.getId());
        assertEquals("main-app", capturedMessage.getSource());
        assertEquals("ai-app", capturedMessage.getDestination());
        assertEquals("check-answer-for-cheating", capturedMessage.getType());
        assertEquals(questionText + "||" + answerText, capturedMessage.getPayload());
    }
}

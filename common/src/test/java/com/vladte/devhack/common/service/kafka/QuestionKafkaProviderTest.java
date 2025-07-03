package com.vladte.devhack.common.service.kafka;

import com.vladte.devhack.common.service.BaseServiceTest;
import com.vladte.devhack.infra.model.KafkaMessage;
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
 * Unit tests for the QuestionKafkaProvider.
 */
public class QuestionKafkaProviderTest extends BaseServiceTest {

    @Mock
    private KafkaProducerService kafkaProducerService;

    @Captor
    private ArgumentCaptor<KafkaMessage> messageCaptor;

    private QuestionKafkaProvider questionKafkaProvider;

    @BeforeEach
    public void setup() {
        questionKafkaProvider = new QuestionKafkaProvider(kafkaProducerService);
    }


    @Test
    @DisplayName("Send generate questions request with message ID should create correct message and call producer service")
    public void testSendGenerateQuestionsRequestWithMessageId() {
        // Arrange
        String messageId = "test-message-id";
        String tagName = "java";
        int count = 5;
        String difficulty = "Medium";

        CompletableFuture<SendResult<String, KafkaMessage>> future = new CompletableFuture<>();
        when(kafkaProducerService.sendQuestionGenerateRequest(any(KafkaMessage.class))).thenReturn(future);

        // Act
        CompletableFuture<SendResult<String, KafkaMessage>> result =
                questionKafkaProvider.sendGenerateQuestionsRequest(messageId, tagName, count, difficulty);

        // Assert
        assertNotNull(result);
        verify(kafkaProducerService).sendQuestionGenerateRequest(messageCaptor.capture());

        KafkaMessage capturedMessage = messageCaptor.getValue();
        assertEquals(messageId, capturedMessage.getId());
        assertEquals("main-app", capturedMessage.getSource());
        assertEquals("ai-app", capturedMessage.getDestination());
        assertEquals("generate-questions", capturedMessage.getType());
        assertEquals(tagName + "|" + count + "|" + difficulty, capturedMessage.getPayload());
    }
}
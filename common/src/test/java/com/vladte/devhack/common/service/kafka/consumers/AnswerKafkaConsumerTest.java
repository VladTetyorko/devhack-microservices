package com.vladte.devhack.common.service.kafka.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.common.config.KafkaTestConfig;
import com.vladte.devhack.common.service.kafka.concumers.AnswerKafkaConsumer;
import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.arguments.response.AnswerCheckResponseArguments;
import com.vladte.devhack.infra.model.payload.response.AnswerCheckResponsePayload;
import com.vladte.devhack.infra.service.kafka.PendingRequestManager;
import com.vladte.devhack.infra.topics.Topics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for AnswerKafkaConsumer.
 * Tests the Kafka consumer functionality for answer check responses.
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {KafkaTestConfig.class})
@EmbeddedKafka(partitions = 1, topics = {Topics.ANSWER_FEEDBACK_RESULT})
@DirtiesContext
class AnswerKafkaConsumerTest {

    @Mock
    private PendingRequestManager<AnswerCheckResponseArguments> pendingRequestManager;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private ObjectMapper objectMapper;
    private AnswerKafkaConsumer answerKafkaConsumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        answerKafkaConsumer = new AnswerKafkaConsumer(objectMapper, pendingRequestManager);
    }

    @Test
    void testListenWithValidCheatingMessage() {
        // Given
        String messageId = UUID.randomUUID().toString();
        AnswerCheckResponsePayload payload = AnswerCheckResponsePayload.fromCheatingResult(true);

        KafkaMessage<AnswerCheckResponsePayload> message = KafkaMessage.<AnswerCheckResponsePayload>builder()
                .id(messageId)
                .source("ai-module")
                .destination("common-module")
                .type(MessageTypes.CHECK_ANSWER_RESULT.getValue())
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();

        // When
        assertDoesNotThrow(() -> answerKafkaConsumer.listen(message));

        // Then
        verify(pendingRequestManager, times(1)).complete(eq(messageId), any());
    }

    @Test
    void testListenWithValidScoreAndFeedbackMessage() {
        // Given
        String messageId = UUID.randomUUID().toString();
        AnswerCheckResponseArguments arguments = AnswerCheckResponseArguments.builder()
                .hasCheating(false)
                .score(85.5)
                .feedback("Good answer with minor improvements needed")
                .build();

        AnswerCheckResponsePayload payload = AnswerCheckResponsePayload.builder()
                .arguments(arguments)
                .build();

        KafkaMessage<AnswerCheckResponsePayload> message = KafkaMessage.<AnswerCheckResponsePayload>builder()
                .id(messageId)
                .source("ai-module")
                .destination("common-module")
                .type(MessageTypes.CHECK_ANSWER_RESULT.getValue())
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();

        // When
        assertDoesNotThrow(() -> answerKafkaConsumer.listen(message));

        // Then
        verify(pendingRequestManager, times(1)).complete(eq(messageId), any());
    }

    @Test
    void testListenWithErrorMessage() {
        // Given
        String messageId = UUID.randomUUID().toString();
        AnswerCheckResponsePayload payload = AnswerCheckResponsePayload.error("Processing failed");

        KafkaMessage<AnswerCheckResponsePayload> message = KafkaMessage.<AnswerCheckResponsePayload>builder()
                .id(messageId)
                .source("ai-module")
                .destination("common-module")
                .type(MessageTypes.CHECK_ANSWER_RESULT.getValue())
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();

        // When
        assertDoesNotThrow(() -> answerKafkaConsumer.listen(message));

        // Then
        verify(pendingRequestManager, times(1)).complete(eq(messageId), any());
    }

    @Test
    void testListenWithNullMessage() {
        // When & Then
        assertDoesNotThrow(() -> answerKafkaConsumer.listen(null));
        verify(pendingRequestManager, never()).complete(any(), any());
    }

    @Test
    void testListenWithMessageWithoutId() {
        // Given
        AnswerCheckResponsePayload payload = AnswerCheckResponsePayload.fromCheatingResult(true);
        KafkaMessage<AnswerCheckResponsePayload> message = KafkaMessage.<AnswerCheckResponsePayload>builder()
                .source("ai-module")
                .destination("common-module")
                .type(MessageTypes.CHECK_ANSWER_RESULT.getValue())
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();

        // When
        assertDoesNotThrow(() -> answerKafkaConsumer.listen(message));

        // Then - Should handle gracefully even without ID
        verify(pendingRequestManager, times(1)).complete(eq(null), any());
    }

    @Test
    void testAnswerCheckResponseArgumentsGetAsList() {
        // Given
        AnswerCheckResponseArguments arguments = AnswerCheckResponseArguments.builder()
                .hasCheating(true)
                .score(75.0)
                .feedback("Test feedback")
                .build();

        // When
        var result = arguments.getAsList();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("true", result.get(0));
        assertEquals("75.0", result.get(1));
        assertEquals("Test feedback", result.get(2));
    }

    @Test
    void testAnswerCheckResponseArgumentsNecessaryArgumentsAreEmpty() {
        // Given
        AnswerCheckResponseArguments arguments = new AnswerCheckResponseArguments();

        // When
        boolean result = arguments.necessaryArgumentsAreEmpty();

        // Then
        assertFalse(result); // Always returns false according to implementation
    }

    @Test
    void testAnswerCheckResponsePayloadFromCheatingResult() {
        // Given
        Boolean isCheating = true;

        // When
        AnswerCheckResponsePayload payload = AnswerCheckResponsePayload.fromCheatingResult(isCheating);

        // Then
        assertNotNull(payload);
        assertNotNull(payload.getArguments());
        assertTrue(payload.getArguments().isHasCheating());
    }

    @Test
    void testAnswerCheckResponsePayloadError() {
        // Given
        String errorMessage = "Test error message";

        // When
        AnswerCheckResponsePayload payload = AnswerCheckResponsePayload.error(errorMessage);

        // Then
        assertNotNull(payload);
        assertTrue(payload.isHasErrors());
        assertEquals(errorMessage, payload.getErrorMessage());
    }
}
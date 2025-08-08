package com.vladte.devhack.common.service.kafka.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.common.config.KafkaTestConfig;
import com.vladte.devhack.common.service.kafka.concumers.QuestionKafkaConsumer;
import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.arguments.response.QuestionGenerateResponseArguments;
import com.vladte.devhack.infra.model.payload.response.QuestionGenerateResponsePayload;
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
 * Test class for QuestionKafkaConsumer.
 * Tests the Kafka consumer functionality for question generation responses.
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {KafkaTestConfig.class})
@EmbeddedKafka(partitions = 1, topics = {Topics.QUESTION_GENERATE_RESULT})
@DirtiesContext
class QuestionKafkaConsumerTest {

    @Mock
    private PendingRequestManager<QuestionGenerateResponseArguments> pendingRequestManager;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private ObjectMapper objectMapper;
    private QuestionKafkaConsumer questionKafkaConsumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        questionKafkaConsumer = new QuestionKafkaConsumer(objectMapper, pendingRequestManager);
    }

    @Test
    void testListenWithValidGeneratedQuestionsMessage() {
        // Given
        String messageId = UUID.randomUUID().toString();
        String generatedText = "What is Java?\nExplain OOP principles\nDescribe Spring Framework";
        QuestionGenerateResponsePayload payload = QuestionGenerateResponsePayload.fromGeneratedText(generatedText);

        KafkaMessage<QuestionGenerateResponsePayload> message = KafkaMessage.<QuestionGenerateResponsePayload>builder()
                .id(messageId)
                .source("ai-module")
                .destination("common-module")
                .type(MessageTypes.QUESTION_GENERATE_RESULT.getValue())
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();

        // When
        assertDoesNotThrow(() -> questionKafkaConsumer.listen(message));

        // Then
        verify(pendingRequestManager, times(1)).complete(eq(messageId), any());
    }

    @Test
    void testListenWithValidQuestionsArrayMessage() {
        // Given
        String messageId = UUID.randomUUID().toString();
        String[] questions = {"What is polymorphism?", "Explain inheritance", "Define encapsulation"};
        QuestionGenerateResponseArguments arguments = QuestionGenerateResponseArguments.builder()
                .questions(questions)
                .build();

        QuestionGenerateResponsePayload payload = QuestionGenerateResponsePayload.builder()
                .arguments(arguments)
                .build();

        KafkaMessage<QuestionGenerateResponsePayload> message = KafkaMessage.<QuestionGenerateResponsePayload>builder()
                .id(messageId)
                .source("ai-module")
                .destination("common-module")
                .type(MessageTypes.QUESTION_GENERATE_RESULT.getValue())
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();

        // When
        assertDoesNotThrow(() -> questionKafkaConsumer.listen(message));

        // Then
        verify(pendingRequestManager, times(1)).complete(eq(messageId), any());
    }

    @Test
    void testListenWithErrorMessage() {
        // Given
        String messageId = UUID.randomUUID().toString();
        QuestionGenerateResponsePayload payload = QuestionGenerateResponsePayload.error("Question generation failed");

        KafkaMessage<QuestionGenerateResponsePayload> message = KafkaMessage.<QuestionGenerateResponsePayload>builder()
                .id(messageId)
                .source("ai-module")
                .destination("common-module")
                .type(MessageTypes.QUESTION_GENERATE_RESULT.getValue())
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();

        // When
        assertDoesNotThrow(() -> questionKafkaConsumer.listen(message));

        // Then
        verify(pendingRequestManager, times(1)).complete(eq(messageId), any());
    }

    @Test
    void testListenWithNullMessage() {
        // When & Then
        assertDoesNotThrow(() -> questionKafkaConsumer.listen(null));
        verify(pendingRequestManager, never()).complete(any(), any());
    }

    @Test
    void testListenWithMessageWithoutId() {
        // Given
        QuestionGenerateResponsePayload payload = QuestionGenerateResponsePayload.fromGeneratedText("Test question?");
        KafkaMessage<QuestionGenerateResponsePayload> message = KafkaMessage.<QuestionGenerateResponsePayload>builder()
                .source("ai-module")
                .destination("common-module")
                .type(MessageTypes.QUESTION_GENERATE_RESULT.getValue())
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();

        // When
        assertDoesNotThrow(() -> questionKafkaConsumer.listen(message));

        // Then - Should handle gracefully even without ID
        verify(pendingRequestManager, times(1)).complete(eq(null), any());
    }

    @Test
    void testQuestionGenerateResponseArgumentsGetAsList() {
        // Given
        String[] questions = {"Question 1?", "Question 2?", "Question 3?"};
        QuestionGenerateResponseArguments arguments = QuestionGenerateResponseArguments.builder()
                .questions(questions)
                .build();

        // When
        var result = arguments.getAsList();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Question 1?", result.get(0));
        assertEquals("Question 2?", result.get(1));
        assertEquals("Question 3?", result.get(2));
    }

    @Test
    void testQuestionGenerateResponseArgumentsNecessaryArgumentsAreEmpty() {
        // Given
        QuestionGenerateResponseArguments arguments = new QuestionGenerateResponseArguments();

        // When
        boolean result = arguments.necessaryArgumentsAreEmpty();

        // Then
        assertFalse(result); // Always returns false according to implementation
    }

    @Test
    void testQuestionGenerateResponsePayloadFromGeneratedText() {
        // Given
        String generatedText = "What is Spring Boot?\nExplain dependency injection\nDescribe MVC pattern";

        // When
        QuestionGenerateResponsePayload payload = QuestionGenerateResponsePayload.fromGeneratedText(generatedText);

        // Then
        assertNotNull(payload);
        assertNotNull(payload.getArguments());
        assertNotNull(payload.getArguments().getQuestions());
        assertEquals(3, payload.getArguments().getQuestions().length);
        assertEquals("What is Spring Boot?", payload.getArguments().getQuestions()[0]);
        assertEquals("Explain dependency injection", payload.getArguments().getQuestions()[1]);
        assertEquals("Describe MVC pattern", payload.getArguments().getQuestions()[2]);
    }

    @Test
    void testQuestionGenerateResponsePayloadError() {
        // Given
        String errorMessage = "Invalid payload format";

        // When
        QuestionGenerateResponsePayload payload = QuestionGenerateResponsePayload.error(errorMessage);

        // Then
        assertNotNull(payload);
        assertTrue(payload.isHasErrors());
        assertEquals(errorMessage, payload.getErrorMessage());
    }

    @Test
    void testQuestionGenerateResponsePayloadFromGeneratedTextWithSingleQuestion() {
        // Given
        String generatedText = "What is Java?";

        // When
        QuestionGenerateResponsePayload payload = QuestionGenerateResponsePayload.fromGeneratedText(generatedText);

        // Then
        assertNotNull(payload);
        assertNotNull(payload.getArguments());
        assertNotNull(payload.getArguments().getQuestions());
        assertEquals(1, payload.getArguments().getQuestions().length);
        assertEquals("What is Java?", payload.getArguments().getQuestions()[0]);
    }

    @Test
    void testQuestionGenerateResponsePayloadFromGeneratedTextWithEmptyString() {
        // Given
        String generatedText = "";

        // When
        QuestionGenerateResponsePayload payload = QuestionGenerateResponsePayload.fromGeneratedText(generatedText);

        // Then
        assertNotNull(payload);
        assertNotNull(payload.getArguments());
        assertNotNull(payload.getArguments().getQuestions());
        assertEquals(1, payload.getArguments().getQuestions().length);
        assertEquals("", payload.getArguments().getQuestions()[0]);
    }
}
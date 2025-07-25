package com.vladte.devhack.common.service.kafka.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.common.config.KafkaTestConfig;
import com.vladte.devhack.common.service.kafka.concumers.VacancyResponseKafkaConsumer;
import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.arguments.response.VacancyParseResultArguments;
import com.vladte.devhack.infra.model.payload.response.VacancyParseResponsePayload;
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
 * Test class for VacancyResponseKafkaConsumer.
 * Tests the Kafka consumer functionality for vacancy parsing responses.
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {KafkaTestConfig.class})
@EmbeddedKafka(partitions = 1, topics = {Topics.VACANCY_PARSING_RESULT})
@DirtiesContext
class VacancyResponseKafkaConsumerTest {

    @Mock
    private PendingRequestManager<VacancyParseResultArguments> pendingRequestManager;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private ObjectMapper objectMapper;
    private VacancyResponseKafkaConsumer vacancyResponseKafkaConsumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        vacancyResponseKafkaConsumer = new VacancyResponseKafkaConsumer(objectMapper, pendingRequestManager);
    }

    @Test
    void testListenWithValidVacancyJsonMessage() {
        // Given
        String messageId = UUID.randomUUID().toString();
        String vacancyJson = "{\"title\":\"Software Engineer\",\"company\":\"Tech Corp\",\"requirements\":[\"Java\",\"Spring\"],\"salary\":\"80000\"}";
        VacancyParseResponsePayload payload = VacancyParseResponsePayload.fromJson(vacancyJson);

        KafkaMessage<VacancyParseResponsePayload> message = KafkaMessage.<VacancyParseResponsePayload>builder()
                .id(messageId)
                .source("ai-module")
                .destination("common-module")
                .type(MessageTypes.VACANCY_PARSING_RESULT.getValue())
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();

        // When
        assertDoesNotThrow(() -> vacancyResponseKafkaConsumer.listen(message));

        // Then
        verify(pendingRequestManager, times(1)).complete(eq(messageId), any());
    }

    @Test
    void testListenWithValidVacancyArgumentsMessage() {
        // Given
        String messageId = UUID.randomUUID().toString();
        String vacancyJson = "{\"position\":\"Backend Developer\",\"location\":\"Remote\",\"skills\":[\"Python\",\"Django\"]}";
        VacancyParseResultArguments arguments = VacancyParseResultArguments.builder()
                .vacancyJson(vacancyJson)
                .build();

        VacancyParseResponsePayload payload = VacancyParseResponsePayload.builder()
                .arguments(arguments)
                .build();

        KafkaMessage<VacancyParseResponsePayload> message = KafkaMessage.<VacancyParseResponsePayload>builder()
                .id(messageId)
                .source("ai-module")
                .destination("common-module")
                .type(MessageTypes.VACANCY_PARSING_RESULT.getValue())
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();

        // When
        assertDoesNotThrow(() -> vacancyResponseKafkaConsumer.listen(message));

        // Then
        verify(pendingRequestManager, times(1)).complete(eq(messageId), any());
    }

    @Test
    void testListenWithErrorMessage() {
        // Given
        String messageId = UUID.randomUUID().toString();
        VacancyParseResponsePayload payload = VacancyParseResponsePayload.error("Vacancy parsing failed");

        KafkaMessage<VacancyParseResponsePayload> message = KafkaMessage.<VacancyParseResponsePayload>builder()
                .id(messageId)
                .source("ai-module")
                .destination("common-module")
                .type(MessageTypes.VACANCY_PARSING_RESULT.getValue())
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();

        // When
        assertDoesNotThrow(() -> vacancyResponseKafkaConsumer.listen(message));

        // Then
        verify(pendingRequestManager, times(1)).complete(eq(messageId), any());
    }

    @Test
    void testListenWithNullMessage() {
        // When & Then
        assertDoesNotThrow(() -> vacancyResponseKafkaConsumer.listen(null));
        verify(pendingRequestManager, never()).complete(any(), any());
    }

    @Test
    void testListenWithMessageWithoutId() {
        // Given
        VacancyParseResponsePayload payload = VacancyParseResponsePayload.fromJson("{\"test\":\"data\"}");
        KafkaMessage<VacancyParseResponsePayload> message = KafkaMessage.<VacancyParseResponsePayload>builder()
                .source("ai-module")
                .destination("common-module")
                .type(MessageTypes.VACANCY_PARSING_RESULT.getValue())
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();

        // When
        assertDoesNotThrow(() -> vacancyResponseKafkaConsumer.listen(message));

        // Then - Should handle gracefully even without ID
        verify(pendingRequestManager, times(1)).complete(eq(null), any());
    }

    @Test
    void testVacancyParseResultArgumentsGetAsList() {
        // Given
        String vacancyJson = "{\"title\":\"Data Scientist\",\"company\":\"AI Corp\"}";
        VacancyParseResultArguments arguments = VacancyParseResultArguments.builder()
                .vacancyJson(vacancyJson)
                .build();

        // When
        var result = arguments.getAsList();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(vacancyJson, result.get(0));
    }

    @Test
    void testVacancyParseResultArgumentsNecessaryArgumentsAreEmpty() {
        // Given
        VacancyParseResultArguments arguments = new VacancyParseResultArguments();

        // When
        boolean result = arguments.necessaryArgumentsAreEmpty();

        // Then
        assertFalse(result); // Always returns false according to implementation
    }

    @Test
    void testVacancyParseResponsePayloadFromJson() {
        // Given
        String vacancyJson = "{\"title\":\"Full Stack Developer\",\"experience\":\"3+ years\",\"technologies\":[\"React\",\"Node.js\"]}";

        // When
        VacancyParseResponsePayload payload = VacancyParseResponsePayload.fromJson(vacancyJson);

        // Then
        assertNotNull(payload);
        assertNotNull(payload.getArguments());
        assertEquals(vacancyJson, payload.getArguments().getVacancyJson());
        assertFalse(payload.isHasErrors());
    }

    @Test
    void testVacancyParseResponsePayloadError() {
        // Given
        String errorMessage = "Failed to parse vacancy data";

        // When
        VacancyParseResponsePayload payload = VacancyParseResponsePayload.error(errorMessage);

        // Then
        assertNotNull(payload);
        assertTrue(payload.isHasErrors());
        assertEquals(errorMessage, payload.getErrorMessage());
    }

    @Test
    void testVacancyParseResponsePayloadFromJsonWithEmptyString() {
        // Given
        String vacancyJson = "";

        // When
        VacancyParseResponsePayload payload = VacancyParseResponsePayload.fromJson(vacancyJson);

        // Then
        assertNotNull(payload);
        assertNotNull(payload.getArguments());
        assertEquals("", payload.getArguments().getVacancyJson());
        assertFalse(payload.isHasErrors());
    }

    @Test
    void testVacancyParseResponsePayloadFromJsonWithComplexJson() {
        // Given
        String vacancyJson = "{\"title\":\"Senior Java Developer\",\"company\":\"Enterprise Solutions\",\"location\":\"New York\",\"salary\":{\"min\":90000,\"max\":120000,\"currency\":\"USD\"},\"requirements\":[\"Java 11+\",\"Spring Boot\",\"Microservices\",\"Docker\"],\"benefits\":[\"Health Insurance\",\"401k\",\"Remote Work\"]}";

        // When
        VacancyParseResponsePayload payload = VacancyParseResponsePayload.fromJson(vacancyJson);

        // Then
        assertNotNull(payload);
        assertNotNull(payload.getArguments());
        assertEquals(vacancyJson, payload.getArguments().getVacancyJson());
        assertFalse(payload.isHasErrors());
    }

    @Test
    void testVacancyParseResultArgumentsWithNullJson() {
        // Given
        VacancyParseResultArguments arguments = VacancyParseResultArguments.builder()
                .vacancyJson(null)
                .build();

        // When
        var result = arguments.getAsList();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0));
    }
}
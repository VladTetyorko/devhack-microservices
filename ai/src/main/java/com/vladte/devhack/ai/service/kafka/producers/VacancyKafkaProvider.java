package com.vladte.devhack.ai.service.kafka.producers;

import com.vladte.devhack.infra.model.payload.response.VacancyParseResponsePayload;

/**
 * Service for sending vacancy parsing result messages via Kafka.
 */
public interface VacancyKafkaProvider {
    /**
     * Sends a parsed vacancy result message.
     *
     * @param messageId The unique identifier for the message
     * @param payload   The vacancy parsing response payload to send
     */
    void sendParsedVacancyResult(String messageId, VacancyParseResponsePayload payload);
}

package com.vladte.devhack.ai.service.kafka.producers;

import com.vladte.devhack.infra.model.payload.response.QuestionGenerateResponsePayload;

/**
 * Service for sending question generation result messages via Kafka.
 */
public interface QuestionKafkaProvider {
    /**
     * Sends a question generation result message.
     *
     * @param messageId The unique identifier for the message
     * @param payload   The question generation response payload to send
     */
    void sendQuestionGenerateResult(String messageId, QuestionGenerateResponsePayload payload);
}

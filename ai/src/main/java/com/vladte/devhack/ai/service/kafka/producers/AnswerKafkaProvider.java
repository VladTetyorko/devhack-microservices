package com.vladte.devhack.ai.service.kafka.producers;

import com.vladte.devhack.infra.model.payload.AiResponsePayload;

/**
 * Service for sending answer feedback result messages via Kafka.
 */
public interface AnswerKafkaProvider<T extends AiResponsePayload<?>> {
    /**
     * Sends an answer feedback result message.
     *
     * @param payload The answer feedback result payload to send
     */
    void sendAnswerFeedbackResult(String messageId, T payload);
}

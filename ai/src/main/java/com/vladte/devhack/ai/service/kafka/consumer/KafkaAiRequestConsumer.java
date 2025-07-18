package com.vladte.devhack.ai.service.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.payload.RequestPayload;
import com.vladte.devhack.infra.model.payload.ResponsePayload;
import com.vladte.devhack.infra.service.kafka.consumer.KafkaMessageProcessor;
import com.vladte.devhack.infra.service.kafka.producer.publish.KafkaResponsePublisher;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for AI module Kafka consumers.
 *
 * @param <KafkaMessageRequestPayload>  The type of request payload expected
 * @param <KafkaMessageResponsePayload> The type of response payload to return
 */
@Slf4j
public abstract class KafkaAiRequestConsumer<KafkaMessageRequestPayload extends RequestPayload<?>, KafkaMessageResponsePayload extends ResponsePayload<?>>
        extends KafkaMessageProcessor<KafkaMessageRequestPayload> {

    private final KafkaResponsePublisher<KafkaMessageResponsePayload> responsePublisher;

    protected KafkaAiRequestConsumer(KafkaResponsePublisher<KafkaMessageResponsePayload> responsePublisher,
                                     ObjectMapper objectMapper,
                                     Class<KafkaMessageRequestPayload> requestPayloadClass) {
        super(objectMapper, requestPayloadClass);
        this.responsePublisher = responsePublisher;
    }

    protected abstract KafkaMessageResponsePayload performAiRequest(KafkaMessage<KafkaMessageRequestPayload> message);

    protected abstract KafkaMessageResponsePayload createErrorResponse(String message);

    @Override
    protected void processIncomingMessage(KafkaMessage<KafkaMessageRequestPayload> message) {
        log.info("Processing AI request with ID: {}", message.getId());
        try {
            KafkaMessageResponsePayload response = performAiRequest(message);
            responsePublisher.buildAndSend(message.getId(), response);
        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage(), e);
            responsePublisher.buildAndSend(message.getId(), createErrorResponse("Internal error: " + e.getMessage()));
        }
    }

}

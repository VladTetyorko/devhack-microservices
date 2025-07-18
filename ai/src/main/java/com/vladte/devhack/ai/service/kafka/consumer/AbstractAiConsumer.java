package com.vladte.devhack.ai.service.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.payload.AiRequestPayload;
import com.vladte.devhack.infra.model.payload.AiResponsePayload;
import com.vladte.devhack.infra.service.kafka.AbstractConsumer;
import com.vladte.devhack.infra.service.kafka.AbstractKafkaResponder;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for AI module Kafka consumers.
 *
 * @param <RequestPayload>  The type of request payload expected
 * @param <ResponsePayload> The type of response payload to return
 */
@Slf4j
public abstract class AbstractAiConsumer<RequestPayload extends AiRequestPayload<?>, ResponsePayload extends AiResponsePayload<?>>
        extends AbstractConsumer<RequestPayload> {

    private final AbstractKafkaResponder<ResponsePayload> responder;

    protected AbstractAiConsumer(AbstractKafkaResponder<ResponsePayload> responder,
                                 ObjectMapper objectMapper,
                                 Class<RequestPayload> requestPayloadClass) {
        super(objectMapper, requestPayloadClass);
        this.responder = responder;
    }

    @Override
    protected void handleMessage(KafkaMessage<RequestPayload> message) {
        log.info("Processing AI request with ID: {}", message.getId());
        try {
            ResponsePayload response = handleRequest(message);
            responder.sendResponse(message.getId(), response);
        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage(), e);
            responder.sendResponse(message.getId(), createErrorResponse("Internal error: " + e.getMessage()));
        }
    }

    /**
     * Subclass should implement this to produce a response.
     */
    protected abstract ResponsePayload handleRequest(KafkaMessage<RequestPayload> message);

    /**
     * Subclass should implement error response creation.
     */
    protected abstract ResponsePayload createErrorResponse(String message);
}

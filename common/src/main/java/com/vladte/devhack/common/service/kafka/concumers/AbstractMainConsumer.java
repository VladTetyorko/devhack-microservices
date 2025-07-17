package com.vladte.devhack.common.service.kafka.concumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.common.service.kafka.util.PendingRequestManager;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.arguments.KafkaPayloadArguments;
import com.vladte.devhack.infra.model.payload.AiResponsePayload;
import com.vladte.devhack.infra.service.kafka.AbstractConsumer;

/**
 * Abstract base class for application module Kafka consumers.
 * Extends the base AbstractConsumer and provides app-specific functionality
 * like pending request management and sending support via producer.
 *
 * @param <ResponseArguments> Type of response arguments
 * @param <ResponsePayload>   Type of response payload containing T
 */
public abstract class AbstractMainConsumer<ResponseArguments extends KafkaPayloadArguments, ResponsePayload extends AiResponsePayload<ResponseArguments>> extends AbstractConsumer<ResponsePayload> {

    private final PendingRequestManager<ResponseArguments> pendingRequestManager;

    public AbstractMainConsumer(ObjectMapper objectMapper,
                                Class<ResponsePayload> responsePayloadClass, PendingRequestManager<ResponseArguments> pendingRequestManager) {
        super(objectMapper, responsePayloadClass);
        this.pendingRequestManager = pendingRequestManager;
    }

    @Override
    protected void handleMessage(KafkaMessage<ResponsePayload> message) {
        String messageId = message.getId();
        var payload = message.getPayload();
        if (payload.isHasErrors()) {
            pendingRequestManager.completeExceptionally(
                    messageId,
                    new RuntimeException("Kafka error: " + payload.getErrorMessage())
            );
        } else {
            pendingRequestManager.complete(messageId, payload.getArguments());
        }
    }


    /**
     * Check if the message is an expected response type.
     *
     * @param message the message to check
     * @return true if the message should be processed, false otherwise
     */
    protected abstract boolean isExpectedResponse(KafkaMessage<ResponsePayload> message);
}

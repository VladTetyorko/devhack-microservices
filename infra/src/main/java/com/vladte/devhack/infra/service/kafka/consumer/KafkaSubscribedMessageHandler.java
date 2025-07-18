package com.vladte.devhack.infra.service.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.arguments.KafkaPayloadArguments;
import com.vladte.devhack.infra.service.kafka.PendingRequestManager;

/**
 * Abstract base class for application module Kafka consumers.
 * Extends the base AbstractConsumer and provides app-specific functionality
 * like pending request management and sending support via producer.
 *
 * @param <ResponseArguments> Type of response arguments
 * @param <ResponsePayload>   Type of response payload containing T
 */
public abstract class KafkaSubscribedMessageHandler<ResponseArguments extends KafkaPayloadArguments, ResponsePayload extends com.vladte.devhack.infra.model.payload.ResponsePayload<ResponseArguments>>
        extends KafkaMessageProcessor<ResponsePayload> {

    private final PendingRequestManager<ResponseArguments> pendingRequestManager;

    public KafkaSubscribedMessageHandler(ObjectMapper objectMapper,
                                         Class<ResponsePayload> responsePayloadClass,
                                         PendingRequestManager<ResponseArguments> pendingRequestManager) {
        super(objectMapper, responsePayloadClass);
        this.pendingRequestManager = pendingRequestManager;
    }

    protected abstract boolean isExpectedResponse(KafkaMessage<ResponsePayload> message);

    @Override
    protected void processIncomingMessage(KafkaMessage<ResponsePayload> message) {
        String messageId = message.getId();
        var payload = message.getPayload();
        if (payload.isHasErrors() || !isExpectedResponse(message)) {
            pendingRequestManager.completeExceptionally(
                    messageId,
                    new RuntimeException("Kafka error: " + payload.getErrorMessage())
            );
        } else {
            pendingRequestManager.complete(messageId, payload.getArguments());
        }
    }
}

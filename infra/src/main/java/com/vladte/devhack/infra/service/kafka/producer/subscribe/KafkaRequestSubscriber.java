package com.vladte.devhack.infra.service.kafka.producer.subscribe;

import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.arguments.KafkaPayloadArguments;
import com.vladte.devhack.infra.model.payload.RequestPayload;
import com.vladte.devhack.infra.service.kafka.KafkaMessageSender;
import com.vladte.devhack.infra.service.kafka.PendingRequestManager;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

public abstract class KafkaRequestSubscriber<RequestMessagePayload extends RequestPayload<?>, ResponseArguments extends KafkaPayloadArguments> extends KafkaMessageSender<RequestMessagePayload> {

    private final PendingRequestManager<ResponseArguments> pendingManager;

    protected KafkaRequestSubscriber(KafkaTemplate<String, KafkaMessage<RequestMessagePayload>> kafkaTemplate,
                                     PendingRequestManager<ResponseArguments> pendingManager) {
        super(kafkaTemplate);
        this.pendingManager = pendingManager;
    }

    public CompletableFuture<ResponseArguments> subscribeToResponse(String messageId, RequestMessagePayload payload) {
        CompletableFuture<ResponseArguments> future = pendingManager.register(messageId);
        buildAndSend(messageId, payload)
                .exceptionally(ex -> {
                    pendingManager.completeExceptionally(messageId, ex);
                    return null;
                });
        return future;
    }
}

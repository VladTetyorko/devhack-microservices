package com.vladte.devhack.common.service.kafka.producers;

import com.vladte.devhack.common.service.kafka.util.PendingRequestManager;
import com.vladte.devhack.infra.message.MessageDestinations;
import com.vladte.devhack.infra.message.MessageSources;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.arguments.KafkaPayloadArguments;
import com.vladte.devhack.infra.model.payload.AiRequestPayload;
import com.vladte.devhack.infra.service.kafka.KafkaProducerService;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractMainKafkaProviderService<
        RequestPayload extends AiRequestPayload<?>,
        ResponseArguments extends KafkaPayloadArguments> {

    private final KafkaProducerService<RequestPayload> kafkaProducerService;
    private final PendingRequestManager<ResponseArguments> pendingRequestManager;

    protected AbstractMainKafkaProviderService(
            KafkaProducerService<RequestPayload> kafkaProducerService,
            PendingRequestManager<ResponseArguments> pendingRequestManager
    ) {
        this.kafkaProducerService = kafkaProducerService;
        this.pendingRequestManager = pendingRequestManager;
    }

    public CompletableFuture<ResponseArguments> sendRequest(String messageId, RequestPayload payload) {
        KafkaMessage<RequestPayload> message = KafkaMessage.<RequestPayload>builder()
                .id(messageId)
                .source(MessageSources.MAIN_APP)
                .destination(MessageDestinations.AI_APP)
                .type(getMessageType())
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();

        CompletableFuture<ResponseArguments> future = pendingRequestManager.register(messageId);

        kafkaProducerService.sendMessage(getTopic(), message)
                .thenAccept(sendResult -> {
                })
                .exceptionally(ex -> {
                    pendingRequestManager.completeExceptionally(messageId, ex);
                    return null;
                });

        return future;
    }

    protected abstract String getTopic();

    protected abstract String getMessageType();
}

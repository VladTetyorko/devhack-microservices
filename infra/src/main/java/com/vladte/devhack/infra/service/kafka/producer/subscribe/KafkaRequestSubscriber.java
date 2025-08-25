package com.vladte.devhack.infra.service.kafka.producer.subscribe;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.entities.global.ai.AiPrompt;
import com.vladte.devhack.infra.ai.PromptEngine;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.arguments.KafkaPayloadArguments;
import com.vladte.devhack.infra.model.payload.request.AiRenderedRequestPayload;
import com.vladte.devhack.infra.service.kafka.KafkaMessageSender;
import com.vladte.devhack.infra.service.kafka.PendingRequestManager;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

public abstract class KafkaRequestSubscriber<ResponseArguments extends KafkaPayloadArguments> extends KafkaMessageSender<AiRenderedRequestPayload> {

    private final PendingRequestManager<ResponseArguments> pendingManager;
    private final PromptEngine promptEngine;

    protected KafkaRequestSubscriber(KafkaTemplate<String, KafkaMessage<AiRenderedRequestPayload>> kafkaTemplate,
                                     PendingRequestManager<ResponseArguments> pendingManager, ObjectMapper objectMapper) {
        super(kafkaTemplate);
        this.pendingManager = pendingManager;
        this.promptEngine = new PromptEngine(objectMapper);
    }

    protected AiRenderedRequestPayload buildAiMessagePayloadFromSources(AiPrompt prompt, Object... sources) {
        return promptEngine.render(prompt, sources);
    }

    public CompletableFuture<ResponseArguments> subscribeToResponse(String messageId, AiRenderedRequestPayload payload) {
        CompletableFuture<ResponseArguments> future = pendingManager.register(messageId);
        super.buildAndSend(messageId, payload)
                .exceptionally(ex -> {
                    pendingManager.completeExceptionally(messageId, ex);
                    return null;
                });
        return future;
    }
}

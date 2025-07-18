package com.vladte.devhack.infra.service.kafka;

import com.vladte.devhack.infra.model.arguments.KafkaPayloadArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe registry for managing pending requests and their futures.
 * Allows producers and consumers to coordinate using messageId.
 */
@Component
public class PendingRequestManager<PayloadArguments extends KafkaPayloadArguments> {

    private static final Logger log = LoggerFactory.getLogger(PendingRequestManager.class);

    private final Map<String, CompletableFuture<PayloadArguments>> pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<PayloadArguments> register(String messageId) {
        log.info("Registering pending request: {}", messageId);
        CompletableFuture<PayloadArguments> future = new CompletableFuture<>();
        pendingRequests.put(messageId, future);
        return future;
    }

    public void complete(String messageId, PayloadArguments result) {
        CompletableFuture<PayloadArguments> future = pendingRequests.remove(messageId);
        if (future != null) {
            log.info("Completing future for messageId: {}", messageId);
            future.complete(result);
        } else {
            log.warn("No pending future found for messageId: {}", messageId);
        }
    }

    public void completeExceptionally(String messageId, Throwable throwable) {
        CompletableFuture<PayloadArguments> future = pendingRequests.remove(messageId);
        if (future != null) {
            log.info("Completing future exceptionally for messageId: {}", messageId);
            future.completeExceptionally(throwable);
        } else {
            log.warn("No pending future found for messageId (exceptional): {}", messageId);
        }
    }

    public boolean hasPending(String messageId) {
        return pendingRequests.containsKey(messageId);
    }

    public void clear() {
        pendingRequests.clear();
    }
}

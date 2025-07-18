package com.vladte.devhack.ai.service.kafka.producers;

import com.vladte.devhack.infra.model.payload.ResponsePayload;

/**
 * Marker interface for ResponseProviders
 */
public interface KafkaResponseProvider<T extends ResponsePayload<?>> {
}

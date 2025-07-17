package com.vladte.devhack.common.service.kafka.producers;

import com.vladte.devhack.infra.model.arguments.response.VacancyParseResultArguments;

import java.util.concurrent.CompletableFuture;

/**
 * Service for sending vacancy parsing requests to the AI module via Kafka.
 * Uses generic Kafka messages for scalable and type-safe communication.
 */
public interface VacancyResponseKafkaProvider {

    /**
     * Sends a request to parse vacancy response text.
     *
     * @param messageId   the ID to use for the message
     * @param vacancyText the vacancy text to parse
     * @return a CompletableFuture that will be completed when the send operation completes
     */
    CompletableFuture<VacancyParseResultArguments> parseVacancyResponse(
            String messageId, String vacancyText);
}

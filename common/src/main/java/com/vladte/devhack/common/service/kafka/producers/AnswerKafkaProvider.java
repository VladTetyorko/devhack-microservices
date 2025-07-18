package com.vladte.devhack.common.service.kafka.producers;

import com.vladte.devhack.infra.model.arguments.response.AnswerCheckResponseArguments;

import java.util.concurrent.CompletableFuture;

/**
 * Service for sending answer feedback requests to the AI module via Kafka.
 * Uses generic Kafka messages for scalable and type-safe communication.
 */
public interface AnswerKafkaProvider {
    CompletableFuture<AnswerCheckResponseArguments> subscribeToAnswerCheatingCheck(
            String messageId, String questionText, String answerText);

    CompletableFuture<AnswerCheckResponseArguments> subscribeToAnswerFeedbackCheck(
            String messageId, String questionText, String answerText);
}

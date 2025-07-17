package com.vladte.devhack.common.service.kafka.producers;

import com.vladte.devhack.infra.model.arguments.response.QuestionGenerateResponseArguments;

import java.util.concurrent.CompletableFuture;

/**
 * Service for sending question generation requests to the AI module via Kafka.
 * Uses generic Kafka messages for scalable and type-safe communication.
 */
public interface QuestionKafkaProvider {

    /**
     * Sends a request to generate questions for a specific tag, using a pre-generated message ID.
     *
     * @param messageId  the ID to use for the message
     * @param tagName    the name of the tag to generate questions for
     * @param count      the number of questions to generate
     * @param difficulty the difficulty level of the questions
     * @return a CompletableFuture that will be completed when the send operation completes
     */
    CompletableFuture<QuestionGenerateResponseArguments> sendGenerateQuestionsRequest(
            String messageId, String tagName, int count, String difficulty);

}

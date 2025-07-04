package com.vladte.devhack.common.service.kafka.producers;

import com.vladte.devhack.infra.model.KafkaMessage;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

/**
 * Service for sending answer feedback requests to the AI module via Kafka.
 */
public interface AnswerKafkaProvider {

    /**
     * Sends a request to check if an answer contains evidence of cheating, using a pre-generated message ID.
     *
     * @param messageId    the ID to use for the message
     * @param questionText the text of the question
     * @param answerText   the text of the answer to check
     * @return a CompletableFuture that will be completed when the send operation completes
     */
    CompletableFuture<SendResult<String, KafkaMessage>> sendAnswerCheatingCheckRequest(
            String messageId, String questionText, String answerText);

    /**
     * Sends a request to check an answer with AI and get feedback, using a pre-generated message ID.
     *
     * @param messageId    the ID to use for the message
     * @param questionText the text of the question
     * @param answerText   the text of the answer to check
     * @return a CompletableFuture that will be completed when the send operation completes
     */
    CompletableFuture<SendResult<String, KafkaMessage>> sendAnswerFeedbackRequest(
            String messageId, String questionText, String answerText);
}

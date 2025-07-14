package com.vladte.devhack.common.service.kafka.producers.impl;

import com.vladte.devhack.common.service.kafka.KafkaProducerService;
import com.vladte.devhack.common.service.kafka.producers.AnswerKafkaProvider;
import com.vladte.devhack.infra.message.MessageDestinations;
import com.vladte.devhack.infra.message.MessageSources;
import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for sending answer feedback requests to the AI module via Kafka.
 */
@Service
public class AnswerKafkaProviderImpl implements AnswerKafkaProvider {

    private static final Logger logger = LoggerFactory.getLogger(AnswerKafkaProviderImpl.class);
    private final KafkaProducerService kafkaProducerService;


    public AnswerKafkaProviderImpl(@Qualifier("MainKafkaProducerService") KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @Override
    public CompletableFuture<SendResult<String, KafkaMessage>> sendAnswerCheatingCheckRequest(
            String messageId, String questionText, String answerText) {
        logger.info("Sending request to check answer for cheating with ID: {}", messageId);

        // Format the payload as expected by the AI module
        String payload = questionText + "||" + answerText;

        // Create the Kafka message with the specified ID
        KafkaMessage message = new KafkaMessage(
                messageId,
                MessageSources.MAIN_APP,
                MessageDestinations.AI_APP,
                MessageTypes.CHECK_ANSWER_FOR_CHEATING.getValue(),
                payload,
                java.time.LocalDateTime.now()
        );

        // Send the message to the AI module
        return kafkaProducerService.sendMessage(message);
    }


    @Override
    public CompletableFuture<SendResult<String, KafkaMessage>> sendAnswerFeedbackRequest(
            String messageId, String questionText, String answerText) {
        logger.info("Sending request to check answer with AI with ID: {}", messageId);

        // Format the payload as expected by the AI module
        String payload = questionText + "||" + answerText;

        // Create the Kafka message with the specified ID
        KafkaMessage message = new KafkaMessage(
                messageId,
                MessageSources.MAIN_APP,
                MessageDestinations.AI_APP,
                MessageTypes.CHECK_ANSWER_WITH_FEEDBACK.getValue(),
                payload,
                java.time.LocalDateTime.now()
        );

        return kafkaProducerService.sendMessage(message);
    }


}

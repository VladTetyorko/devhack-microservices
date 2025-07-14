package com.vladte.devhack.common.service.kafka.producers.impl;

import com.vladte.devhack.common.service.kafka.KafkaProducerService;
import com.vladte.devhack.common.service.kafka.producers.QuestionKafkaProvider;
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
 * Service for sending question generation requests to the AI module via Kafka.
 */
@Service
public class QuestionKafkaProviderImpl implements QuestionKafkaProvider {

    private static final Logger logger = LoggerFactory.getLogger(QuestionKafkaProviderImpl.class);
    private final KafkaProducerService kafkaProducerService;


    public QuestionKafkaProviderImpl(@Qualifier("MainKafkaProducerService") KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    /**
     * Sends a request to generate questions for a specific tag, using a pre-generated message ID.
     *
     * @param messageId  the ID to use for the message
     * @param tagName    the name of the tag to generate questions for
     * @param count      the number of questions to generate
     * @param difficulty the difficulty level of the questions
     * @return a CompletableFuture that will be completed when the send operation completes
     */
    public CompletableFuture<SendResult<String, KafkaMessage>> sendGenerateQuestionsRequest(
            String messageId, String tagName, int count, String difficulty) {
        logger.info("Sending request to generate {} {} difficulty questions for tag: {} with ID: {}",
                count, difficulty, tagName, messageId);

        // Format the payload as expected by the AI module
        String payload = String.format("%s|%d|%s", tagName, count, difficulty);

        // Create the Kafka message with the specified ID
        KafkaMessage message = new KafkaMessage(
                messageId,
                MessageSources.MAIN_APP,
                MessageDestinations.AI_APP,
                MessageTypes.QUESTION_GENERATE.getValue(),
                payload,
                java.time.LocalDateTime.now()
        );

        // Send the message to the AI module
        return kafkaProducerService.sendMessage(message);
    }
}

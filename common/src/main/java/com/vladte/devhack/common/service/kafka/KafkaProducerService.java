package com.vladte.devhack.common.service.kafka;

import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.topics.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for sending messages to Kafka topics.
 */
@Service("MainKafkaProducerService")
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, KafkaMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Sends a message to a Kafka topic.
     *
     * @param message The message to send
     * @return A CompletableFuture that will be completed when the send operation completes
     */
    public CompletableFuture<SendResult<String, KafkaMessage>> sendMessage(KafkaMessage message) {
        logger.debug("Sending message: {}", message);
        String topic;

        switch (MessageTypes.fromValue(message.getType())) {
            case MessageTypes.QUESTION_GENERATE -> topic = Topics.QUESTION_GENERATE_REQUEST;
            case MessageTypes.CHECK_ANSWER_WITH_FEEDBACK,
                 MessageTypes.CHECK_ANSWER_FOR_CHEATING -> topic = Topics.ANSWER_FEEDBACK_REQUEST;
            case MessageTypes.VACANCY_PARSING -> topic = Topics.VACANCY_PARSING_REQUEST;
            default -> {
                logger.error("Unknown message type: {}", message.getType());
                throw new IllegalArgumentException("Unknown message type: " + message.getType());
            }
        }

        return kafkaTemplate.send(topic, message.getId(), message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Message sent successfully: {}", message.getId());
                    } else {
                        logger.error("Failed to send message: {}", ex.getMessage());
                    }
                });
    }
}
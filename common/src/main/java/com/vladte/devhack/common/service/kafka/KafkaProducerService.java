package com.vladte.devhack.common.service.kafka;

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
    private final KafkaTemplate<String, com.vladte.devhack.infra.model.KafkaMessage> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, KafkaMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    /**
     * Sends a question generation request to the AI module.
     *
     * @param message The question generation request message to send
     * @return A CompletableFuture that will be completed when the send operation completes
     */
    public CompletableFuture<SendResult<String, KafkaMessage>> sendQuestionGenerateRequest(KafkaMessage message) {
        logger.info("Sending question generation request: {}", message);
        return kafkaTemplate.send(Topics.QUESTION_GENERATE_REQUEST, message.getId(), message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Question generation request sent successfully: {}", message);
                    } else {
                        logger.error("Failed to send question generation request: {}", ex.getMessage());
                    }
                });
    }

    /**
     * Sends an answer feedback request to the AI module.
     *
     * @param message The answer feedback request message to send
     * @return A CompletableFuture that will be completed when the send operation completes
     */
    public CompletableFuture<SendResult<String, KafkaMessage>> sendAnswerFeedbackRequest(KafkaMessage message) {
        logger.info("Sending answer feedback request: {}", message);
        return kafkaTemplate.send(Topics.ANSWER_FEEDBACK_REQUEST, message.getId(), message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Answer feedback request sent successfully: {}", message);
                    } else {
                        logger.error("Failed to send answer feedback request: {}", ex.getMessage());
                    }
                });
    }
}

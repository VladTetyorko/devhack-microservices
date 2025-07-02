package com.vladte.devhack.ai.service.kafka;

import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.topics.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for sending messages to Kafka topics.
 */
@Service("AiKafkaProducerService")
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, KafkaMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    /**
     * Sends a question generation result message.
     *
     * @param message The question generation result message to send
     */
    public void sendQuestionGenerateResult(KafkaMessage message) {
        logger.info("Sending question generation result: {}", message);
        kafkaTemplate.send(Topics.QUESTION_GENERATE_RESULT, message.getId(), message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Question generation result sent successfully: {}", message);
                    } else {
                        logger.error("Failed to send question generation result: {}", ex.getMessage());
                    }
                });
    }

    /**
     * Sends an answer feedback result message.
     *
     * @param message The answer feedback result message to send
     */
    public void sendAnswerFeedbackResult(KafkaMessage message) {
        logger.info("Sending answer feedback result: {}", message);
        kafkaTemplate.send(Topics.ANSWER_FEEDBACK_RESULT, message.getId(), message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Answer feedback result sent successfully: {}", message);
                    } else {
                        logger.error("Failed to send answer feedback result: {}", ex.getMessage());
                    }
                });
    }
}

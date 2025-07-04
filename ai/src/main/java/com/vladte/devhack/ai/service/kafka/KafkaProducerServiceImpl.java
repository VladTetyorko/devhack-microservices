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
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerServiceImpl.class);
    private final KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    public KafkaProducerServiceImpl(KafkaTemplate<String, KafkaMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    /**
     * Sends a question generation result message.
     *
     * @param message The question generation result message to send
     */
    @Override
    public void sendQuestionGenerateResult(KafkaMessage message) {
        logger.debug("Sending question generation result: {}", message);
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
    @Override
    public void sendAnswerFeedbackResult(KafkaMessage message) {
        logger.debug("Sending answer feedback result: {}", message);
        kafkaTemplate.send(Topics.ANSWER_FEEDBACK_RESULT, message.getId(), message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Answer feedback result sent successfully: {}", message);
                    } else {
                        logger.error("Failed to send answer feedback result: {}", ex.getMessage());
                    }
                });
    }

    /**
     * Sends a parsed vacancy result message.
     *
     * @param message The parsed vacancy result message to send
     */
    @Override
    public void sendParsedVacancyResult(KafkaMessage message) {
        logger.debug("Sending vacancy parsing result: {}", message);
        kafkaTemplate.send(Topics.VACANCY_PARSING_RESULT, message.getId(), message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Vacancy parsing result sent successfully: {}", message);
                    } else {
                        logger.error("Failed to send vacancy parsing result: {}", ex.getMessage());
                    }
                });
    }
}

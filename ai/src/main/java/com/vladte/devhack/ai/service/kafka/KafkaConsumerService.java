package com.vladte.devhack.ai.service.kafka;

import com.vladte.devhack.ai.service.api.OpenAiService;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.topics.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for consuming messages from Kafka topics.
 * This implementation is specific to the AI module and handles AI-related requests.
 */
@Service("AiKafkaConsumer")
public class KafkaConsumerService extends com.vladte.devhack.infra.service.kafka.KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final KafkaProducerService kafkaProducerService;
    private final OpenAiService openAiService;

    public KafkaConsumerService(KafkaProducerService kafkaProducerService,
                                @Qualifier("gptJService") OpenAiService openAiService) {
        this.kafkaProducerService = kafkaProducerService;
        this.openAiService = openAiService;
    }

    /**
     * Consumes question generation request messages.
     *
     * @param message The question generation request message
     */
    @KafkaListener(
            topics = Topics.QUESTION_GENERATE_REQUEST,
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "2"
    )
    public void consumeQuestionGenerateRequest(KafkaMessage message) {
        logger.info("Received question generation request: {}", message);
        String responsePayload = handleGenerateQuestions(message.getPayload());
        sendResponse(message.getId(), responsePayload, "question-generate");
    }

    /**
     * Consumes answer feedback request messages.
     *
     * @param message The answer feedback request message
     */
    @KafkaListener(
            topics = Topics.ANSWER_FEEDBACK_REQUEST,
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "2"
    )
    public void consumeAnswerFeedbackRequest(KafkaMessage message) {
        logger.info("Received answer feedback request: {}", message);
        String responsePayload;

        if ("check-answer-for-cheating".equals(message.getType())) {
            responsePayload = handleCheckAnswerForCheating(message.getPayload());
        } else {
            responsePayload = handleCheckAnswerWithFeedback(message.getPayload());
        }

        sendResponse(message.getId(), responsePayload, "answer-feedback");
    }

    private String handleGenerateQuestions(String payload) {
        logger.debug("Handling generate-questions request");
        String[] parts = payload.split("\\|");
        if (parts.length != 3) {
            logger.error("Invalid payload format for generate-questions: {}", payload);
            return "Error: Invalid payload format for generate-questions";
        }
        String tag = parts[0];
        int count = Integer.parseInt(parts[1]);
        String difficulty = parts[2];
        try {
            return openAiService.generateQuestionsForTagAsync(tag, count, difficulty).join();
        } catch (Exception e) {
            logger.error("Error generating questions: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String handleCheckAnswerWithFeedback(String payload) {
        logger.debug("Handling check-answer-with-feedback request");
        String[] parts = payload.split("\\|\\|");
        if (parts.length != 2) {
            logger.error("Invalid payload format for check-answer-with-feedback: {}", payload);
            return "Error: Invalid payload format for check-answer-with-feedback";
        }
        try {
            Map<String, Object> result = openAiService.checkAnswerWithFeedbackAsync(parts[0], parts[1]).join();
            return "score:" + result.get("score") + "||feedback:" + result.get("feedback");
        } catch (Exception e) {
            logger.error("Error checking answer with feedback: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String handleCheckAnswerForCheating(String payload) {
        logger.debug("Handling check-answer-for-cheating request");
        String[] parts = payload.split("\\|\\|");
        if (parts.length != 2) {
            logger.error("Invalid payload format for check-answer-for-cheating: {}", payload);
            return "Error: Invalid payload format for check-answer-for-cheating";
        }
        try {
            Boolean isCheating = openAiService.checkAnswerForCheatingAsync(parts[0], parts[1]).join();
            return isCheating.toString();
        } catch (Exception e) {
            logger.error("Error checking answer for cheating: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Sends a response for a message with a specific type.
     *
     * @param messageId       The ID of the original message
     * @param responsePayload The response payload
     * @param messageType     The type of the original message
     */
    private void sendResponse(String messageId, String responsePayload, String messageType) {
        logger.debug("Sending response for message ID: {} with type: {}", messageId, messageType);
        KafkaMessage response = KafkaMessage.create(
                "ai-app",
                "main-app",
                "ai-response",
                responsePayload
        );
        response.setId(messageId);

        // Send to the appropriate topic based on the message type
        if (messageType != null && messageType.equals("question-generate")) {
            // Send to question generation result topic
            kafkaProducerService.sendQuestionGenerateResult(response);
        } else {
            // Default to answer feedback result topic
            kafkaProducerService.sendAnswerFeedbackResult(response);
        }
    }

}

package com.vladte.devhack.ai.service.kafka;

import com.vladte.devhack.ai.service.api.OpenAiService;
import com.vladte.devhack.infra.message.MessageDestinations;
import com.vladte.devhack.infra.message.MessageSources;
import com.vladte.devhack.infra.message.MessageTypes;
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
        sendResponse(message.getId(), responsePayload, message.getType());
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

        if (MessageTypes.CHECK_ANSWER_FOR_CHEATING.getValue().equals(message.getType())) {
            responsePayload = handleCheckAnswerForCheating(message.getPayload());
        } else if (MessageTypes.CHECK_ANSWER_WITH_FEEDBACK.getValue().equals(message.getType())) {
            responsePayload = handleCheckAnswerWithFeedback(message.getPayload());
        } else {
            responsePayload = "Error: Invalid message type: " + message.getType();
        }

        sendResponse(message.getId(), responsePayload, message.getType());
    }

    /**
     * Consumes question generation request messages.
     *
     * @param message The question generation request message
     */
    @KafkaListener(
            topics = Topics.VACANCY_PARSING_REQUEST,
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "2"
    )
    public void consumeVacancyParsingRequest(KafkaMessage message) {
        logger.info("Received parse vacancy request: {}", message.getId());
        String responsePayload = handleVacancyParsing(message.getPayload());
        sendResponse(message.getId(), responsePayload, message.getType());
    }


    // helper methods


    /**
     * Sends a response for a message with a specific type.
     *
     * @param messageId       The ID of the original message
     * @param responsePayload The response payload
     * @param messageType     The type of the original message
     */
    private void sendResponse(String messageId, String responsePayload, String messageType) {
        logger.debug("Sending response for message ID: {} with type: {}", messageId, messageType);
        KafkaMessage.KafkaMessageBuilder responseBuilder = KafkaMessage.builder()
                .source(MessageSources.AI_APP)
                .destination(MessageDestinations.MAIN_APP)
                .payload(responsePayload);
        responseBuilder.id(messageId);


        switch (MessageTypes.fromValue(messageType)) {
            case QUESTION_GENERATE ->
                    kafkaProducerService.sendQuestionGenerateResult(
                    responseBuilder
                            .type(MessageTypes.QUESTION_GENERATE_RESULT.getValue())
                            .build());
            case VACANCY_PARSING -> kafkaProducerService.sendParsedVacancyResult(responseBuilder
                    .type(MessageTypes.VACANCY_PARSING_RESULT.getValue())
                    .build());
            case CHECK_ANSWER_FOR_CHEATING, CHECK_ANSWER_WITH_FEEDBACK ->
                    kafkaProducerService.sendAnswerFeedbackResult(responseBuilder
                            .type(MessageTypes.CHECK_ANSWER_RESULT.getValue())
                            .build());
            default -> {
                logger.warn("Unknown message type: {}", messageType);
                kafkaProducerService.sendAnswerFeedbackResult(responseBuilder.build());
            }
        }
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

    private String handleVacancyParsing(String payload) {
        logger.debug("Handling vacancy parsing request");
        try {
            Map<String, Object> result = openAiService.extractVacancyModelFromDescription(payload).join();
            return result.get("data").toString();
        } catch (Exception e) {
            logger.error("Error parsing vacancy description: {}", e.getMessage(), e);
            throw e;
        }
    }
}

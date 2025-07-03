package com.vladte.devhack.common.service.kafka;

import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.topics.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for consuming answer check responses from the AI module via Kafka.
 */
@Service
public class AnswerKafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AnswerKafkaConsumer.class);

    // Map to store pending requests by message ID 
    private final Map<String, CompletableFuture<Map<String, Object>>> pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<Map<String, Object>> registerPendingRequest(String messageId) {
        logger.debug("Registering pending request with message ID: {}", messageId);
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        pendingRequests.put(messageId, future);
        logger.debug("Pending request registered. Current pending requests count: {}", pendingRequests.size());
        return future;
    }

    @KafkaListener(topics = Topics.ANSWER_FEEDBACK_RESULT, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeAnswerFeedbackResult(KafkaMessage message) {
        logger.info("Received answer feedback result: {}", message);
        logger.debug("Processing answer feedback result with ID: {}, type: {}", message.getId(), message.getType());

        if (!isAnswerCheckResponse(message)) {
            logger.debug("Ignoring message with type: {} (not an AI response)", message.getType());
            return;
        }

        processAnswerCheckResponse(message);
    }

    private boolean isAnswerCheckResponse(KafkaMessage message) {
        return MessageTypes.CHECK_ANSWER_RESULT.getValue().equals(message.getType());
    }

    private void processAnswerCheckResponse(KafkaMessage message) {
        CompletableFuture<Map<String, Object>> future = getPendingRequest(message.getId());
        if (future == null) {
            logger.warn("Received response for unknown request: {}", message.getId());
            return;
        }

        processResponsePayload(message.getId(), message.getPayload(), future);
    }

    private CompletableFuture<Map<String, Object>> getPendingRequest(String messageId) {
        logger.debug("Message is an AI response, checking for pending request with ID: {}", messageId);
        return pendingRequests.remove(messageId);
    }

    private void processResponsePayload(String messageId, String payload, CompletableFuture<Map<String, Object>> future) {
        logger.debug("Found pending request for message ID: {}", messageId);

        if (payload.startsWith("Error:")) {
            handleErrorResponse(messageId, payload, future);
        } else {
            handleSuccessResponse(messageId, payload, future);
        }
    }

    private void handleErrorResponse(String messageId, String payload, CompletableFuture<Map<String, Object>> future) {
        logger.error("Error from AI module: {}", payload);
        future.completeExceptionally(new RuntimeException(payload));
        logger.debug("Completed future exceptionally for message ID: {}", messageId);
    }

    private void handleSuccessResponse(String messageId, String payload, CompletableFuture<Map<String, Object>> future) {
        try {
            logger.debug("Parsing response payload for message ID: {}", messageId);
            Map<String, Object> result = parseResponse(payload);
            future.complete(result);
            logger.info("Successfully completed request for message ID: {}, result: {}", messageId, result);
        } catch (Exception e) {
            logger.error("Error parsing AI response", e);
            future.completeExceptionally(new RuntimeException("Error parsing AI response: " + e.getMessage(), e));
            logger.debug("Completed future exceptionally due to parsing error for message ID: {}", messageId);
        }
    }

    private Map<String, Object> parseResponse(String payload) {
        logger.debug("Parsing response payload: {}", payload);
        Map<String, Object> result = new ConcurrentHashMap<>();

        if (isBooleanPayload(payload)) {
            return parseBooleanPayload(payload, result);
        }

        return parseScoreAndFeedback(payload, result);
    }

    private boolean isBooleanPayload(String payload) {
        return "true".equalsIgnoreCase(payload) || "false".equalsIgnoreCase(payload);
    }

    private Map<String, Object> parseBooleanPayload(String payload, Map<String, Object> result) {
        logger.debug("Payload is a boolean value: {}", payload);
        Boolean isCheating = Boolean.parseBoolean(payload);
        result.put("isCheating", isCheating);
        logger.debug("Extracted cheating result: {}", isCheating);
        return result;
    }

    private Map<String, Object> parseScoreAndFeedback(String payload, Map<String, Object> result) {
        String[] parts = payload.split("\\|\\|");
        logger.debug("Split payload into {} parts", parts.length);

        for (String part : parts) {
            logger.debug("Processing part: {}", part);
            if (part.startsWith("score:")) {
                parseScore(part, result);
            } else if (part.startsWith("feedback:")) {
                parseFeedback(part, result);
            } else {
                logger.warn("Unrecognized response part: {}", part);
            }
        }

        logger.debug("Completed parsing response, result: {}", result);
        return result;
    }

    private void parseScore(String part, Map<String, Object> result) {
        String scoreStr = part.substring(6);
        logger.debug("Extracted score string: {}", scoreStr);
        try {
            Double score = Double.parseDouble(scoreStr);
            result.put("score", score);
            logger.debug("Successfully parsed score: {}", score);
        } catch (NumberFormatException e) {
            logger.error("Error parsing score: {}", scoreStr, e);
            result.put("score", 0.0);
            logger.debug("Using default score 0.0 due to parsing error");
        }
    }

    private void parseFeedback(String part, Map<String, Object> result) {
        String feedback = part.substring(9);
        result.put("feedback", feedback);
        logger.debug("Extracted feedback: {}", feedback);
    }
}
package com.vladte.devhack.common.service.kafka;

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

    /**
     * Registers a pending request for answer checking.
     *
     * @param messageId the ID of the message sent to the AI module
     * @return a CompletableFuture that will be completed when the response is received
     */
    public CompletableFuture<Map<String, Object>> registerPendingRequest(String messageId) {
        logger.debug("Registering pending request with message ID: {}", messageId);
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        pendingRequests.put(messageId, future);
        logger.debug("Pending request registered. Current pending requests count: {}", pendingRequests.size());
        return future;
    }


    /**
     * Consumes answer feedback result messages from the AI module.
     *
     * @param message The answer feedback result message received from the AI module
     */
    @KafkaListener(topics = Topics.ANSWER_FEEDBACK_RESULT, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeAnswerFeedbackResult(KafkaMessage message) {
        logger.info("Received answer feedback result: {}", message);
        logger.debug("Processing answer feedback result with ID: {}, type: {}", message.getId(), message.getType());

        // Check if this is a response to an answer check request
        if ("ai-response".equals(message.getType())) {
            logger.debug("Message is an AI response, checking for pending request with ID: {}", message.getId());
            CompletableFuture<Map<String, Object>> future = pendingRequests.remove(message.getId());

            if (future != null) {
                // This is a response to a pending request
                logger.debug("Found pending request for message ID: {}", message.getId());
                String payload = message.getPayload();

                if (payload.startsWith("Error:")) {
                    // This is an error response
                    logger.error("Error from AI module: {}", payload);
                    future.completeExceptionally(new RuntimeException(payload));
                    logger.debug("Completed future exceptionally for message ID: {}", message.getId());
                } else {
                    try {
                        // Parse the response
                        logger.debug("Parsing response payload for message ID: {}", message.getId());
                        Map<String, Object> result = parseResponse(payload);

                        // Complete the future with the parsed result
                        future.complete(result);
                        logger.info("Successfully completed request for message ID: {}, result: {}", message.getId(), result);
                    } catch (Exception e) {
                        logger.error("Error parsing AI response", e);
                        future.completeExceptionally(new RuntimeException("Error parsing AI response: " + e.getMessage(), e));
                        logger.debug("Completed future exceptionally due to parsing error for message ID: {}", message.getId());
                    }
                }
            } else {
                logger.warn("Received response for unknown request: {}", message.getId());
            }
        } else {
            logger.debug("Ignoring message with type: {} (not an AI response)", message.getType());
        }
    }

    /**
     * Parses the response from the AI module.
     * The response format is expected to be "score:[score]||feedback:[feedback]"
     * or a boolean value for cheating checks.
     *
     * @param payload the response payload
     * @return a map containing the score and feedback, or a boolean for cheating checks
     */
    private Map<String, Object> parseResponse(String payload) {
        logger.debug("Parsing response payload: {}", payload);
        Map<String, Object> result = new ConcurrentHashMap<>();

        // Check if the payload is a boolean value (for cheating checks)
        if ("true".equalsIgnoreCase(payload) || "false".equalsIgnoreCase(payload)) {
            logger.debug("Payload is a boolean value: {}", payload);
            Boolean isCheating = Boolean.parseBoolean(payload);
            result.put("isCheating", isCheating);
            logger.debug("Extracted cheating result: {}", isCheating);
            return result;
        }

        // Otherwise, parse as score and feedback
        String[] parts = payload.split("\\|\\|");
        logger.debug("Split payload into {} parts", parts.length);

        for (String part : parts) {
            logger.debug("Processing part: {}", part);
            if (part.startsWith("score:")) {
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
            } else if (part.startsWith("feedback:")) {
                String feedback = part.substring(9);
                result.put("feedback", feedback);
                logger.debug("Extracted feedback: {}", feedback);
            } else {
                logger.warn("Unrecognized response part: {}", part);
            }
        }

        logger.debug("Completed parsing response, result: {}", result);
        return result;
    }
}

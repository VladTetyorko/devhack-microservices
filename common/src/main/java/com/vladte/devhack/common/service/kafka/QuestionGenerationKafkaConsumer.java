package com.vladte.devhack.common.service.kafka;

import com.vladte.devhack.common.model.QuestionGenerationResponse;
import com.vladte.devhack.common.service.generations.QuestionParsingService;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.topics.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for consuming question generation responses from the AI module via Kafka.
 */
@Service
public class QuestionGenerationKafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(QuestionGenerationKafkaConsumer.class);
    private final QuestionParsingService questionParsingService;

    // Map to store pending requests by message ID
    private final Map<String, CompletableFuture<QuestionGenerationResponse>> pendingRequests = new ConcurrentHashMap<>();

    @Autowired
    public QuestionGenerationKafkaConsumer(QuestionParsingService questionParsingService) {
        this.questionParsingService = questionParsingService;
    }

    /**
     * Registers a pending request for question generation.
     *
     * @param messageId the ID of the message sent to the AI module
     * @return a CompletableFuture that will be completed when the response is received
     */
    public CompletableFuture<QuestionGenerationResponse> registerPendingRequest(String messageId) {
        CompletableFuture<QuestionGenerationResponse> future = new CompletableFuture<>();
        pendingRequests.put(messageId, future);
        return future;
    }

    /**
     * Consumes messages from the AI module.
     *
     * @param message The message received from the AI module
     */
    @KafkaListener(topics = Topics.QUESTION_GENERATE_RESULT, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeFromAi(KafkaMessage message) {
        logger.info("Received message from AI: {}", message);

        // Check if this is a response to a question generation request
        if ("ai-response".equals(message.getType())) {
            CompletableFuture<QuestionGenerationResponse> future = pendingRequests.remove(message.getId());

            if (future != null) {
                // This is a response to a pending request
                String payload = message.getPayload();

                if (payload.startsWith("Error:")) {
                    // This is an error response
                    future.complete(QuestionGenerationResponse.error(null, null, payload));
                } else {
                    try {
                        // Parse the generated text into questions
                        List<String> questionTexts = questionParsingService.parseQuestionTexts(payload);

                        // Complete the future with the parsed questions
                        future.complete(QuestionGenerationResponse.success(null, null, questionTexts));
                    } catch (Exception e) {
                        logger.error("Error parsing question texts", e);
                        future.complete(QuestionGenerationResponse.error(null, null,
                                "Error parsing question texts: " + e.getMessage()));
                    }
                }
            } else {
                logger.warn("Received response for unknown request: {}", message.getId());
            }
        }
    }
}
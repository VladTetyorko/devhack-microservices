package com.vladte.devhack.ai.service.api;

import com.vladte.devhack.infra.model.payload.request.AiRenderedRequestPayload;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for interacting with the OpenAI API.
 */
public interface OpenAiService {

    /**
     * Generate interview questions based on a tag asynchronously.
     *
     * @param payload the payload containing question generation data
     * @return a CompletableFuture containing the generated questions
     */
    CompletableFuture<String> generateQuestionsForTagAsync(AiRenderedRequestPayload payload);

    /**
     * Check an answer to an interview question and provide a score and feedback asynchronously.
     *
     * @param payload the payload containing the question and answer to check
     * @return a CompletableFuture containing a map with the score and feedback
     */
    CompletableFuture<Map<String, Object>> checkAnswerWithFeedbackAsync(AiRenderedRequestPayload payload);

    /**
     * Check if an answer to an interview question contains evidence of cheating asynchronously.
     *
     * @param payload the payload containing the question and answer to check for cheating
     * @return a CompletableFuture containing true if the answer contains evidence of cheating, false otherwise
     */
    CompletableFuture<Boolean> checkAnswerForCheatingAsync(AiRenderedRequestPayload payload);

    /**
     * Extract a structured vacancy model from a text description asynchronously.
     *
     * @param payload the payload containing the vacancy text description
     * @return a CompletableFuture containing a map with the extracted vacancy model data
     */
    CompletableFuture<Map<String, Object>> extractVacancyModelFromDescription(AiRenderedRequestPayload payload);
}
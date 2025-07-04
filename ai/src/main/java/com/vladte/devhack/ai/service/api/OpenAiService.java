package com.vladte.devhack.ai.service.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for interacting with the OpenAI API.
 */
public interface OpenAiService {

    /**
     * Generate text using the OpenAI API asynchronously.
     *
     * @param prompt the prompt to send to the API
     * @return a CompletableFuture containing the generated text
     */
    CompletableFuture<String> generateTextAsync(String prompt);

    /**
     * Generate interview questions based on a tag asynchronously.
     *
     * @param tag        the tag to generate questions for
     * @param count      the number of questions to generate
     * @param difficulty the difficulty level of the questions
     * @return a CompletableFuture containing the generated questions
     */
    CompletableFuture<String> generateQuestionsForTagAsync(String tag, int count, String difficulty);

    /**
     * Check an answer to an interview question and provide a score and feedback asynchronously.
     *
     * @param questionText the text of the interview question
     * @param answerText   the text of the answer to check
     * @return a CompletableFuture containing a map with the score and feedback
     */
    CompletableFuture<Map<String, Object>> checkAnswerWithFeedbackAsync(String questionText, String answerText);

    /**
     * Check if an answer to an interview question contains evidence of cheating asynchronously.
     *
     * @param questionText the text of the interview question
     * @param answerText   the text of the answer to check
     * @return a CompletableFuture containing true if the answer contains evidence of cheating, false otherwise
     */
    CompletableFuture<Boolean> checkAnswerForCheatingAsync(String questionText, String answerText);

    CompletableFuture<Map<String, Object>> extractVacancyModelFromDescription(String vacancyDescription);
}

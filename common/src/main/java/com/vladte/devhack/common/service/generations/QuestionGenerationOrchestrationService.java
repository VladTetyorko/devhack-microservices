package com.vladte.devhack.common.service.generations;

import com.vladte.devhack.domain.entities.global.InterviewQuestion;
import com.vladte.devhack.domain.entities.global.Tag;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for orchestrating question generation operations.
 * This interface follows the Single Responsibility Principle by focusing only on orchestrating
 * the generation of questions and related operations.
 */
public interface QuestionGenerationOrchestrationService {

    /**
     * Validate tag name input.
     *
     * @param tagName the name of the tag to validate
     * @return true if the tag name is valid, false otherwise
     */
    boolean isTagInvalid(String tagName);

    /**
     * Start the asynchronous generation of questions for a tag.
     *
     * @param tagName    the name of the tag to generate questions for
     * @param count      the number of questions to generate
     * @param difficulty the difficulty level of the questions
     * @return a CompletableFuture containing the list of generated questions
     */
    @Async
    CompletableFuture<List<InterviewQuestion>> startQuestionGeneration(String tagName, int count, String difficulty);

    /**
     * Start the asynchronous generation of easy questions for a tag.
     *
     * @param tagName the name of the tag to generate questions for
     * @return a CompletableFuture containing the list of generated questions
     */
    @Async
    CompletableFuture<List<InterviewQuestion>> startEasyQuestionGeneration(String tagName);

    /**
     * Find a tag by name.
     *
     * @param tagName the name of the tag to find
     * @return an Optional containing the tag if found, empty otherwise
     */
    Optional<Tag> findTagByName(String tagName);

    /**
     * Build a response map for API question generation.
     *
     * @param success whether the operation was successful
     * @param message the message to include in the response
     * @return a map containing the response data
     */
    Map<String, Object> buildApiResponse(boolean success, String message);

    /**
     * Start the asynchronous generation of easy questions for multiple tags.
     * This method is async and independent, it doesn't return any result.
     *
     * @param tagIds the IDs of the tags to generate questions for
     */
    @Async
    void startEasyQuestionGenerationForMultipleTags(List<UUID> tagIds);


    /**
     * Build a success message for question generation.
     *
     * @param count      the number of questions being generated
     * @param difficulty the difficulty level of the questions
     * @param tagName    the name of the tag
     * @return a success message string
     */
    String buildGenerationSuccessMessage(int count, String difficulty, String tagName);

    /**
     * Build a success message for easy question generation.
     *
     * @param tagName the name of the tag
     * @return a success message string
     */
    String buildEasyGenerationSuccessMessage(String tagName);

    /**
     * Build a success message for multi-tag easy question generation.
     *
     * @param tagCount the number of tags being processed
     * @return a success message string
     */
    String buildMultiTagEasyGenerationSuccessMessage(int tagCount);
}

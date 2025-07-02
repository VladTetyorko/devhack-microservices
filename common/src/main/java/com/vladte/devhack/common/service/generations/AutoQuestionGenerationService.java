package com.vladte.devhack.common.service.generations;

import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.Tag;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for automatically generating easy interview questions for tags using AI.
 */
public interface AutoQuestionGenerationService {

    /**
     * Generate 3 easy interview questions for a specific tag using OpenAI.
     * This method processes one tag per operation and makes one request to OpenAI per tag.
     *
     * @param tag the tag to generate questions for
     * @return a CompletableFuture containing the list of generated questions
     */
    CompletableFuture<List<InterviewQuestion>> generateEasyQuestionsForTag(Tag tag);

    /**
     * Generate 3 easy interview questions for a specific tag name using OpenAI.
     * This method processes one tag per operation and makes one request to OpenAI per tag.
     *
     * @param tagName the name of the tag to generate questions for
     * @return a CompletableFuture containing the list of generated questions
     */
    CompletableFuture<List<InterviewQuestion>> generateEasyQuestionsForTagName(String tagName);

    /**
     * Generate 3 easy interview questions for multiple tags using OpenAI.
     * This method processes each tag separately and makes one request to OpenAI per tag.
     *
     * @param tagIds the IDs of the tags to generate questions for
     * @return a CompletableFuture containing the list of all generated questions
     */
    CompletableFuture<List<InterviewQuestion>> generateEasyQuestionsForMultipleTags(List<UUID> tagIds);
}

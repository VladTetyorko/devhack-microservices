package com.vladte.devhack.common.service.generations.impl;

import com.vladte.devhack.common.service.domain.TagService;
import com.vladte.devhack.common.service.generations.QuestionGenerationOrchestrationService;
import com.vladte.devhack.common.service.generations.QuestionGenerationService;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the QuestionGenerationOrchestrationService interface.
 * This class orchestrates the generation of questions and related operations.
 */
@Service
public class QuestionGenerationOrchestrationServiceImpl implements QuestionGenerationOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(QuestionGenerationOrchestrationServiceImpl.class);

    private final TagService tagService;
    private final QuestionGenerationService questionGenerationService;


    public QuestionGenerationOrchestrationServiceImpl(
            TagService tagService,
            QuestionGenerationService questionGenerationService
    ) {
        this.tagService = tagService;
        this.questionGenerationService = questionGenerationService;
    }

    @Override
    public boolean validateTagName(String tagName) {
        return StringUtils.hasText(tagName);
    }

    @Override
    public CompletableFuture<List<InterviewQuestion>> startQuestionGeneration(String tagName, int count, String difficulty) {
        log.info("Starting asynchronous generation of {} {} difficulty questions for tag: {}",
                count, difficulty, tagName);

        return questionGenerationService.generateAndSaveQuestions(tagName, count, difficulty)
                .thenApply(questions -> {
                    log.info("Successfully generated {} questions for tag: {}", questions.size(), tagName);
                    return questions;
                })
                .exceptionally(ex -> {
                    // Log the error but don't block the user
                    log.error("Error generating questions: {}", ex.getMessage(), ex);
                    return null;
                });
    }

    @Override
    public CompletableFuture<List<InterviewQuestion>> startEasyQuestionGeneration(String tagName) {
        log.info("Starting asynchronous generation of 3 easy questions for tag: {}", tagName);

        // Create a single future for the tag to optimize processing
        CompletableFuture<List<InterviewQuestion>> future = startQuestionGeneration(tagName, 3, "easy");

        // Apply optimizations for this specific tag
        return future.thenApply(questions -> {
            log.info("Successfully generated {} easy questions for tag: {}",
                    questions != null ? questions.size() : 0, tagName);
            return questions;
        }).exceptionally(ex -> {
            log.error("Error generating easy questions for tag {}: {}", tagName, ex.getMessage(), ex);
            return null;
        });
    }

    @Override
    public Optional<Tag> findTagByName(String tagName) {
        return tagService.findTagByName(tagName);
    }

    @Override
    public String buildGenerationSuccessMessage(int count, String difficulty, String tagName) {
        return String.format("Started generating %d %s difficulty questions for tag '%s'. They will appear shortly.",
                count, difficulty, tagName);
    }

    @Override
    public String buildEasyGenerationSuccessMessage(String tagName) {
        return String.format("Started auto-generating 3 easy questions for tag '%s'. They will appear shortly.",
                tagName);
    }

    @Override
    public Map<String, Object> buildApiResponse(boolean success, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        if (success) {
            response.put("message", message);
        } else {
            response.put("error", message);
        }
        return response;
    }

    @Override
    public void startEasyQuestionGenerationForMultipleTags(List<UUID> tagIds) {
        log.info("Starting asynchronous generation of easy questions for multiple tags: {}", tagIds);

        // Use CompletableFuture.runAsync to make this method truly async and independent
        CompletableFuture.runAsync(() -> {
            List<CompletableFuture<List<InterviewQuestion>>> futures = new ArrayList<>();

            for (UUID tagId : tagIds) {
                tagService.findById(tagId).ifPresent(tag -> {
                    String tagName = tag.getName();
                    futures.add(startQuestionGeneration(tagName, 3, "easy"));
                });
            }

            // Wait for all futures to complete, but don't return anything
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenAccept(v -> {
                        int totalQuestions = 0;
                        for (CompletableFuture<List<InterviewQuestion>> future : futures) {
                            List<InterviewQuestion> questions = future.join();
                            if (questions != null) {
                                totalQuestions += questions.size();
                            }
                        }
                        log.info("Successfully generated a total of {} questions for {} tags",
                                totalQuestions, tagIds.size());
                    })
                    .exceptionally(ex -> {
                        log.error("Error generating questions for multiple tags: {}", ex.getMessage(), ex);
                        return null;
                    });
        });
    }

    @Override
    public String buildMultiTagEasyGenerationSuccessMessage(int tagCount) {
        return String.format("Started auto-generating easy questions for %d tags. They will appear shortly.",
                tagCount);
    }
}

package com.vladte.devhack.common.service.generations.impl;

import com.vladte.devhack.common.service.domain.global.TagService;
import com.vladte.devhack.common.service.generations.QuestionGenerationOrchestrationService;
import com.vladte.devhack.common.service.generations.QuestionGenerationService;
import com.vladte.devhack.entities.global.InterviewQuestion;
import com.vladte.devhack.entities.global.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class QuestionGenerationOrchestrationServiceImpl
        implements QuestionGenerationOrchestrationService {

    private static final Logger log =
            LoggerFactory.getLogger(QuestionGenerationOrchestrationServiceImpl.class);

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
    public boolean isTagInvalid(String tagName) {
        return !StringUtils.hasText(tagName);
    }

    @Override
    public CompletableFuture<List<InterviewQuestion>> startQuestionGeneration(
            String tagName, int count, String difficulty
    ) {
        return dispatchGeneration(tagName, count, difficulty);
    }

    @Override
    public CompletableFuture<List<InterviewQuestion>> startEasyQuestionGeneration(
            String tagName
    ) {
        return dispatchGeneration(tagName, 3, "easy");
    }

    @Override
    public Optional<Tag> findTagByName(String tagName) {
        return tagService.findTagByName(tagName);
    }

    @Override
    public String buildGenerationSuccessMessage(
            int count, String difficulty, String tagName
    ) {
        return String.format(
                "Started generating %d %s difficulty questions for tag '%s'. They will appear shortly.",
                count, difficulty, tagName
        );
    }

    @Override
    public String buildEasyGenerationSuccessMessage(String tagName) {
        return buildGenerationSuccessMessage(3, "easy", tagName);
    }

    @Override
    public Map<String, Object> buildApiResponse(boolean success, String message) {
        var response = new HashMap<String, Object>();
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
        CompletableFuture
                .allOf(
                        tagIds.stream()
                                .map(tagService::findById)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(Tag::getName)
                                .map(name -> dispatchGeneration(name, 3, "easy"))
                                .toArray(CompletableFuture[]::new)
                )
                .thenAccept(v -> log.info(
                        "Completed multi‑tag easy generation for {} tags",
                        tagIds.size()
                ))
                .exceptionally(ex -> {
                    log.error("Error generating easy questions for tags {}: {}",
                            tagIds, ex.getMessage(), ex);
                    return null;
                });
    }

    @Override
    public String buildMultiTagEasyGenerationSuccessMessage(int tagCount) {
        return String.format(
                "Started auto‑generating easy questions for %d tags. They will appear shortly.",
                tagCount
        );
    }

    /**
     * Private helper to centralize the common “send to AI → save → log → error‑handle” flow.
     */
    private CompletableFuture<List<InterviewQuestion>> dispatchGeneration(
            String tagName, int count, String difficulty
    ) {
        log.info("Invoking AI to generate {} {} questions for tag '{}'",
                count, difficulty, tagName);

        return questionGenerationService
                .generateAndSaveQuestions(tagName, count, difficulty)
                .thenApply(questions -> {
                    log.info("AI returned {} questions for tag '{}'",
                            questions.size(), tagName);
                    return questions;
                })
                .exceptionally(ex -> {
                    log.error("Generation failed for tag '{}': {}", tagName, ex.getMessage(), ex);
                    // return empty list so caller isn’t blocked by exception
                    return Collections.emptyList();
                });
    }
}

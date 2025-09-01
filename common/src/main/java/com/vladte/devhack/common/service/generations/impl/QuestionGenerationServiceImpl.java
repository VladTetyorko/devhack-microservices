package com.vladte.devhack.common.service.generations.impl;

import com.vladte.devhack.common.service.generations.QuestionGenerationService;
import com.vladte.devhack.common.service.kafka.producers.QuestionKafkaProvider;
import com.vladte.devhack.domain.entities.global.InterviewQuestion;
import com.vladte.devhack.domain.entities.global.Tag;
import com.vladte.devhack.domain.service.global.InterviewQuestionService;
import com.vladte.devhack.domain.service.global.TagService;
import com.vladte.devhack.domain.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the QuestionGenerationService interface for generating interview questions using AI.
 */
@Service
public class QuestionGenerationServiceImpl implements QuestionGenerationService {

    private static final Logger log = LoggerFactory.getLogger(QuestionGenerationServiceImpl.class);
    private final QuestionGenerationService self;
    private final TagService tagService;
    private final UserService userService;
    private final InterviewQuestionService questionService;
    private final QuestionKafkaProvider questionKafkaProvider;

    private final static String QUESTION_AI_PREFIX = "Question: ";

    public QuestionGenerationServiceImpl(
            TagService tagService,
            UserService userService,
            InterviewQuestionService questionService,
            @Lazy QuestionGenerationService self,
            QuestionKafkaProvider questionKafkaProvider) {
        this.tagService = tagService;
        this.userService = userService;
        this.questionService = questionService;
        this.self = self;
        this.questionKafkaProvider = questionKafkaProvider;
    }

    /**
     * Generate interview questions based on a tag and save them to the database.
     *
     * @param tagName    the name of the tag to generate questions for
     * @param count      the number of questions to generate
     * @param difficulty the difficulty level of the questions
     * @return the list of generated questions
     */
    @Async
    public CompletableFuture<List<InterviewQuestion>> generateAndSaveQuestions(
            String tagName, int count, String difficulty) {

        log.info("Generate {} {} questions for tag '{}'", count, difficulty, tagName);

        final Tag tag = findOrCreateTag(tagName);
        final String messageId = UUID.randomUUID().toString();

        return questionKafkaProvider
                .subscribeToQuestionGeneration(messageId, tagName, count, difficulty)
                .orTimeout(30, TimeUnit.SECONDS)                        // avoid hanging forever
                .thenApply(response -> {
                    String[] arr = Objects.requireNonNull(response.getQuestions(),
                            "No questions generated");
                    return Arrays.stream(arr)
                            .map(this::mapAiGeneratedTestToQuestion)    // String -> question text
                            .toList();
                })
                .thenApply(questionTexts ->
                        self.saveQuestionsToDatabase(questionTexts, difficulty, tag))
                .whenComplete((res, ex) -> {
                    if (ex == null) {
                        log.info("Saved {} questions for '{}'", res.size(), tagName);
                    } else {
                        log.error("Failed to generate/save questions for '{}'", tagName, ex);
                    }
                });
    }


    /**
     * Save the generated questions to the database.
     * This method is separated to allow for transaction management.
     *
     * @param questionTexts  the list of question texts to save
     * @param difficulty     the difficulty level of the questions
     * @param tag            the tag to associate with the questions
     */
    @Transactional
    @Override
    public List<InterviewQuestion> saveQuestionsToDatabase(List<String> questionTexts, String difficulty, Tag tag) {
        List<InterviewQuestion> savedQuestions = new ArrayList<>();
        for (String questionText : questionTexts) {
            log.debug("Processing question: {}", questionText.length() > 50 ? questionText.substring(0, 47) + "..." : questionText);

            InterviewQuestion question = new InterviewQuestion();
            question.setQuestionText(questionText);
            question.setDifficulty(difficulty);
            question.setUser(userService.getSystemUser());
            // Add the tag to the question
            Set<Tag> tags = new HashSet<>();
            tags.add(tag);
            question.setTags(tags);

            // Save the question
            InterviewQuestion savedQuestion = questionService.save(question);
            savedQuestions.add(savedQuestion);
            log.debug("Saved question with ID: {}", savedQuestion.getId());
        }
        return savedQuestions;
    }


    private String mapAiGeneratedTestToQuestion(String aiText) {
        if (aiText.startsWith(QUESTION_AI_PREFIX)) {
            return aiText.substring(QUESTION_AI_PREFIX.length());
        } else return aiText;
    }

    /**
     * Find an existing tag by name or create a new one if it doesn't exist.
     *
     * @param tagName the name of the tag
     * @return the tag entity
     */
    private Tag findOrCreateTag(String tagName) {
        log.debug("Finding or creating tag: {}", tagName);
        Optional<Tag> existingTag = tagService.findTagByName(tagName);
        if (existingTag.isPresent()) {
            log.debug("Found existing tag: {}", tagName);
            return existingTag.get();
        } else {
            log.info("Creating new tag: {}", tagName);
            Tag newTag = new Tag();
            newTag.setName(tagName);
            Tag savedTag = tagService.save(newTag);
            log.debug("Created new tag with ID: {}", savedTag.getId());
            return savedTag;
        }
    }
}

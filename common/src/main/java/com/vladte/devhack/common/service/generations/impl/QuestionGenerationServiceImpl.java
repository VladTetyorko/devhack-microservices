package com.vladte.devhack.common.service.generations.impl;

import com.vladte.devhack.common.service.domain.InterviewQuestionService;
import com.vladte.devhack.common.service.domain.TagService;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.common.service.generations.QuestionGenerationService;
import com.vladte.devhack.common.service.kafka.concumers.QuestionKafkaConsumer;
import com.vladte.devhack.common.service.kafka.producers.QuestionKafkaProvider;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.Tag;
import com.vladte.devhack.infra.model.arguments.response.QuestionGenerateResponseArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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
    private final QuestionKafkaConsumer kafkaConsumer;
    private final QuestionKafkaProvider questionKafkaProvider;


    public QuestionGenerationServiceImpl(
            TagService tagService,
            UserService userService,
            InterviewQuestionService questionService,
            @Lazy QuestionGenerationService self,
            QuestionKafkaProvider questionKafkaProvider,
            QuestionKafkaConsumer kafkaConsumer) {
        this.tagService = tagService;
        this.userService = userService;
        this.questionService = questionService;
        this.self = self;
        this.questionKafkaProvider = questionKafkaProvider;
        this.kafkaConsumer = kafkaConsumer;
    }

    /**
     * Generate interview questions based on a tag and save them to the database.
     *
     * @param tagName    the name of the tag to generate questions for
     * @param count      the number of questions to generate
     * @param difficulty the difficulty level of the questions
     * @return the list of generated questions
     */
    @Override
    @Async
    public CompletableFuture<List<InterviewQuestion>> generateAndSaveQuestions(String tagName, int count, String difficulty) {
        log.info("Starting to generate {} {} difficulty questions for tag: {}", count, difficulty, tagName);

        // Find or create the tag
        Tag tag = findOrCreateTag(tagName);
        log.debug("Using tag: {}", tag.getName());

        // Generate questions using AI module via Kafka
        log.info("Sending question generation request to AI module via Kafka");
        try {
            // Generate a message ID
            String messageId = java.util.UUID.randomUUID().toString();

            // Send the message to the AI module
            CompletableFuture<QuestionGenerateResponseArguments> responseFuture = questionKafkaProvider.subscribeToQuestionGeneration(messageId, tagName, count, difficulty);
            log.debug("Sent question generation request to AI module with ID: {}", messageId);

            // Wait for the response from the AI module
            QuestionGenerateResponseArguments response = responseFuture.join();
            log.debug("Received response from AI module: {}", response);

            // Get the questions array from the response
            String[] questions = response.getQuestions();

            if (questions == null || questions.length == 0) {
                throw new RuntimeException("No questions generated");
            }

            // Convert array to list
            List<String> questionTexts = List.of(questions);
            log.info("Received {} questions from AI module", questionTexts.size());

            // Create and save question entities
            List<InterviewQuestion> savedQuestions = new ArrayList<>();
            log.info("Saving questions to database");

            self.saveQuestionsToDatabase(questionTexts, difficulty, tag, savedQuestions);

            log.info("Successfully generated and saved {} questions for tag: {}", savedQuestions.size(), tagName);
            return CompletableFuture.completedFuture(savedQuestions);
        } catch (Exception e) {
            log.error("Error while processing generated questions", e);
            CompletableFuture<List<InterviewQuestion>> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Failed to process generated questions: " + e.getMessage(), e));
            return future;
        }
    }

    /**
     * Save the generated questions to the database.
     * This method is separated to allow for transaction management.
     *
     * @param questionTexts  the list of question texts to save
     * @param difficulty     the difficulty level of the questions
     * @param tag            the tag to associate with the questions
     * @param savedQuestions the list to add the saved questions to
     */
    @Transactional
    @Override
    public void saveQuestionsToDatabase(List<String> questionTexts, String difficulty, Tag tag, List<InterviewQuestion> savedQuestions) {
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

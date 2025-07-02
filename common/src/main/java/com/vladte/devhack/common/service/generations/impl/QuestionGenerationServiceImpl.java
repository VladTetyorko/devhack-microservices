package com.vladte.devhack.common.service.generations.impl;

import com.vladte.devhack.common.model.QuestionGenerationResponse;
import com.vladte.devhack.common.service.domain.InterviewQuestionService;
import com.vladte.devhack.common.service.domain.TagService;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.common.service.generations.QuestionGenerationService;
import com.vladte.devhack.common.service.kafka.QuestionGenerationKafkaConsumer;
import com.vladte.devhack.common.service.kafka.QuestionKafkaProvider;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final Logger logger = LoggerFactory.getLogger(QuestionGenerationServiceImpl.class);
    private final QuestionGenerationService self;
    private final TagService tagService;
    private final UserService userService;
    private final InterviewQuestionService questionService;
    private final QuestionGenerationKafkaConsumer kafkaConsumer;
    private final QuestionKafkaProvider questionKafkaProvider;

    @Autowired
    public QuestionGenerationServiceImpl(
            TagService tagService,
            UserService userService,
            InterviewQuestionService questionService,
            @Lazy QuestionGenerationService self,
            QuestionKafkaProvider questionKafkaProvider,
            QuestionGenerationKafkaConsumer kafkaConsumer) {
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
        logger.info("Starting to generate {} {} difficulty questions for tag: {}", count, difficulty, tagName);

        // Find or create the tag
        Tag tag = findOrCreateTag(tagName);
        logger.debug("Using tag: {}", tag.getName());

        // Generate questions using AI module via Kafka
        logger.info("Sending question generation request to AI module via Kafka");
        try {
            // Generate a message ID
            String messageId = java.util.UUID.randomUUID().toString();

            // Register the pending request with the Kafka consumer
            CompletableFuture<QuestionGenerationResponse> responseFuture = kafkaConsumer.registerPendingRequest(messageId);

            // Send the message to the AI module
            questionKafkaProvider.sendGenerateQuestionsRequest(messageId, tagName, count, difficulty);
            logger.debug("Sent question generation request to AI module with ID: {}", messageId);

            // Wait for the response from the AI module
            QuestionGenerationResponse response = responseFuture.join();
            logger.debug("Received response from AI module: {}", response);

            if ("error".equals(response.getStatus())) {
                throw new RuntimeException("Error from AI module: " + response.getErrorMessage());
            }

            // Get the question texts from the response
            List<String> questionTexts = response.getQuestionTexts();
            logger.info("Received {} questions from AI module", questionTexts.size());

            // Create and save question entities
            List<InterviewQuestion> savedQuestions = new ArrayList<>();
            logger.info("Saving questions to database");

            self.saveQuestionsToDatabase(questionTexts, difficulty, tag, savedQuestions);

            logger.info("Successfully generated and saved {} questions for tag: {}", savedQuestions.size(), tagName);
            return CompletableFuture.completedFuture(savedQuestions);
        } catch (Exception e) {
            logger.error("Error while processing generated questions", e);
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
            logger.debug("Processing question: {}", questionText.length() > 50 ? questionText.substring(0, 47) + "..." : questionText);

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
            logger.debug("Saved question with ID: {}", savedQuestion.getId());
        }
    }

    /**
     * Find an existing tag by name or create a new one if it doesn't exist.
     *
     * @param tagName the name of the tag
     * @return the tag entity
     */
    private Tag findOrCreateTag(String tagName) {
        logger.debug("Finding or creating tag: {}", tagName);
        Optional<Tag> existingTag = tagService.findTagByName(tagName);
        if (existingTag.isPresent()) {
            logger.debug("Found existing tag: {}", tagName);
            return existingTag.get();
        } else {
            logger.info("Creating new tag: {}", tagName);
            Tag newTag = new Tag();
            newTag.setName(tagName);
            Tag savedTag = tagService.save(newTag);
            logger.debug("Created new tag with ID: {}", savedTag.getId());
            return savedTag;
        }
    }
}
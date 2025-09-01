package com.vladte.devhack.common.service.generations.impl;

import com.vladte.devhack.common.service.generations.AnswerOperationsService;
import com.vladte.devhack.common.service.kafka.producers.AnswerKafkaProvider;
import com.vladte.devhack.domain.entities.personalized.Answer;
import com.vladte.devhack.domain.service.personalized.AnswerService;
import com.vladte.devhack.infra.model.arguments.response.AnswerCheckResponseArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class AnswerOperationsServiceImpl implements AnswerOperationsService {

    @Value("${devhack.cheating-check.enabled}")
    private boolean shouldBeCheckedOnCheating;

    private final AnswerService service;
    private final AnswerKafkaProvider answerKafkaProvider;

    private static final Logger log = LoggerFactory.getLogger(AnswerOperationsServiceImpl.class);

    public AnswerOperationsServiceImpl(AnswerService service, AnswerKafkaProvider answerKafkaProvider) {
        this.service = service;
        this.answerKafkaProvider = answerKafkaProvider;
    }

    @Override
    public Answer checkAnswerWithAi(UUID answerId) {
        log.debug("Checking answer with AI for answer ID: {}", answerId);
        Answer answer = findAndValidateAnswer(answerId);

        try {
            if (shouldBeCheckedOnCheating && checkOnCheating(answer)) {
                return handleCheatingDetected(answer);
            }

            return performRegularAnswerCheck(answer);
        } catch (Exception e) {
            log.error("Error while checking answer with AI: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Async
    public CompletableFuture<Answer> checkAnswerWithAiAsync(UUID answerId) {
        log.debug("Starting async check of answer with AI for answer ID: {}", answerId);
        try {
            Answer answer = checkAnswerWithAi(answerId);
            log.debug("Completed async check of answer with AI for answer ID: {}", answerId);
            return CompletableFuture.completedFuture(answer);
        } catch (Exception e) {
            log.error("Error in async check of answer with AI: {}", e.getMessage(), e);
            CompletableFuture<Answer> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    private boolean checkOnCheating(Answer answer) {
        AnswerCheckResponseArguments cheatingResult = performCheatingCheck(answer);
        boolean isCheating = cheatingResult.isHasCheating();
        updateAnswerWithCheatingResult(answer, isCheating);

        return isCheating;
    }

    private Answer findAndValidateAnswer(UUID answerId) {
        return service.findById(answerId)
                .orElseThrow(() -> {
                    log.error("Answer not found with ID: {}", answerId);
                    return new NoSuchElementException("Answer not found with ID: " + answerId);
                });
    }

    private AnswerCheckResponseArguments performCheatingCheck(Answer answer) {
        String questionText = answer.getQuestion().getQuestionText();
        String answerText = answer.getText();
        log.debug("Processing question text (length: {}) and answer text (length: {})",
                questionText.length(), answerText.length());

        String cheatingMessageId = java.util.UUID.randomUUID().toString();
        log.debug("Checking for cheating before evaluating answer");


        CompletableFuture<AnswerCheckResponseArguments> cheatingResponseFuture = answerKafkaProvider.subscribeToAnswerCheatingCheck(cheatingMessageId, questionText, answerText);

        return cheatingResponseFuture.join();
    }

    private void updateAnswerWithCheatingResult(Answer answer, Boolean isCheating) {
        answer.setIsCheating(isCheating);
        log.debug("Updated answer with cheating check result: isCheating={}", isCheating);
    }

    private Answer handleCheatingDetected(Answer answer) {
        log.info("Cheating detected in answer ID: {}. Skipping regular answer check.", answer.getId());
        answer.setAiScore(0.0);
        answer.setAiFeedback("This answer appears to contain evidence of cheating. Please provide your own original answer.");
        return service.save(answer);
    }

    private Answer performRegularAnswerCheck(Answer answer) {
        log.debug("No cheating detected. Proceeding with regular answer check.");
        String messageId = java.util.UUID.randomUUID().toString();


        CompletableFuture<AnswerCheckResponseArguments> responseFuture =
                answerKafkaProvider.subscribeToAnswerFeedbackCheck(messageId,
                        answer.getQuestion(),
                        answer);

        AnswerCheckResponseArguments result = responseFuture.join();

        updateAnswerWithFeedback(answer, result);
        return service.save(answer);
    }

    private void updateAnswerWithFeedback(Answer answer, AnswerCheckResponseArguments result) {
        Double score = result.getScore();
        String feedback = result.getFeedback();
        log.debug("Received AI feedback: score={}, feedback length={}", score, feedback.length());

        answer.setAiScore(score);
        answer.setAiFeedback(feedback);
        log.debug("Updated answer with AI score and feedback");
    }

}

package com.vladte.devhack.common.service.domain.personalized.impl;

import com.vladte.devhack.common.repository.personalized.AnswerRepository;
import com.vladte.devhack.common.service.domain.audit.AuditService;
import com.vladte.devhack.common.service.domain.personalized.AnswerService;
import com.vladte.devhack.common.service.domain.personalized.PersonalizedService;
import com.vladte.devhack.common.service.kafka.producers.AnswerKafkaProvider;
import com.vladte.devhack.entities.global.InterviewQuestion;
import com.vladte.devhack.entities.personalized.Answer;
import com.vladte.devhack.entities.user.User;
import com.vladte.devhack.infra.model.arguments.response.AnswerCheckResponseArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the AnswerService interface.
 */
@Service
public class AnswerServiceImpl extends PersonalizedService<Answer, UUID, AnswerRepository> implements AnswerService {

    @Value("${devhack.cheating-check.enabled}")
    private boolean shouldBeCheckedOnCheating;

    private static final Logger log = LoggerFactory.getLogger(AnswerServiceImpl.class);

    private final AnswerKafkaProvider answerKafkaProvider;

    /**
     * Constructor with repository and service injection.
     *
     * @param repository          the answer repository
     * @param answerKafkaProvider the Answer Kafka provider service
     */

    public AnswerServiceImpl(
            AnswerRepository repository,
            AuditService auditService,
            AnswerKafkaProvider answerKafkaProvider) {
        super(repository, auditService);
        this.answerKafkaProvider = answerKafkaProvider;
    }


    @Override
    public List<Answer> findAnswersByUser(User user) {
        log.debug("Finding answers for user ID: {}", user.getId());
        List<Answer> answers = repository.findByUser(user);
        log.debug("Found {} answers for user ID: {}", answers.size(), user.getId());
        return answers;
    }

    @Override
    public Page<Answer> findAnswersByUser(User user, Pageable pageable) {
        log.debug("Finding answers for user ID: {} with pagination", user.getId());
        Page<Answer> answerPage = repository.findByUser(user, pageable);
        log.debug("Found {} answers for user ID: {} (page {} of {})",
                answerPage.getNumberOfElements(), user.getId(),
                pageable.getPageNumber(), answerPage.getTotalPages());
        return answerPage;
    }

    @Override
    public List<Answer> findAnswersByQuestion(InterviewQuestion question) {
        log.debug("Finding answers for question ID: {}", question.getId());
        List<Answer> answers = repository.findByQuestion(question);
        log.debug("Found {} answers for question ID: {}", answers.size(), question.getId());
        return answers;
    }

    @Override
    public Page<Answer> findAnswersByQuestion(InterviewQuestion question, Pageable pageable) {
        log.debug("Finding answers for question ID: {} with pagination", question.getId());
        Page<Answer> answerPage = repository.findByQuestion(question, pageable);
        log.debug("Found {} answers for question ID: {} (page {} of {})",
                answerPage.getNumberOfElements(), question.getId(),
                pageable.getPageNumber(), answerPage.getTotalPages());
        return answerPage;
    }

    @Override
    public List<Answer> findAnswersByUserAndQuestion(User user, InterviewQuestion question) {
        log.debug("Finding answers for user ID: {} and question ID: {}", user.getId(), question.getId());
        List<Answer> answers = repository.findByUserAndQuestion(user, question);
        log.debug("Found {} answers for user ID: {} and question ID: {}",
                answers.size(), user.getId(), question.getId());
        return answers;
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

    private boolean checkOnCheating(Answer answer) {
        AnswerCheckResponseArguments cheatingResult = performCheatingCheck(answer);
        boolean isCheating = cheatingResult.isHasCheating();
        updateAnswerWithCheatingResult(answer, isCheating);

        return isCheating;
    }

    private Answer findAndValidateAnswer(UUID answerId) {
        return repository.findByIdWithQuestion(answerId)
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
        return save(answer);
    }

    private Answer performRegularAnswerCheck(Answer answer) {
        log.debug("No cheating detected. Proceeding with regular answer check.");
        String messageId = java.util.UUID.randomUUID().toString();


        CompletableFuture<AnswerCheckResponseArguments> responseFuture =
                answerKafkaProvider.subscribeToAnswerFeedbackCheck(messageId,
                        answer.getQuestion().getQuestionText(),
                        answer.getText());

        AnswerCheckResponseArguments result = responseFuture.join();

        updateAnswerWithFeedback(answer, result);
        return save(answer);
    }

    private void updateAnswerWithFeedback(Answer answer, AnswerCheckResponseArguments result) {
        Double score = result.getScore();
        String feedback = result.getFeedback();
        log.debug("Received AI feedback: score={}, feedback length={}", score, feedback.length());

        answer.setAiScore(score);
        answer.setAiFeedback(feedback);
        log.debug("Updated answer with AI score and feedback");
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

    @Override
    protected User getEntityUser(Answer entity) {
        return entity.getUser();
    }
}

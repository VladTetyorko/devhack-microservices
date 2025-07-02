package com.vladte.devhack.common.service.domain.impl;

import com.vladte.devhack.common.repository.AnswerRepository;
import com.vladte.devhack.common.service.domain.AnswerService;
import com.vladte.devhack.common.service.kafka.AnswerKafkaConsumer;
import com.vladte.devhack.common.service.kafka.AnswerKafkaProvider;
import com.vladte.devhack.entities.Answer;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the AnswerService interface.
 */
@Service
public class AnswerServiceImpl extends UserOwnedServiceImpl<Answer, UUID, AnswerRepository> implements AnswerService {

    private static final Logger logger = LoggerFactory.getLogger(AnswerServiceImpl.class);

    private final AnswerService self;
    private final AnswerKafkaProvider answerKafkaProvider;
    private final AnswerKafkaConsumer answerKafkaConsumer;

    /**
     * Constructor with repository and service injection.
     *
     * @param repository          the answer repository
     * @param self                self-reference for async method calls
     * @param answerKafkaProvider the Answer Kafka provider service
     * @param answerKafkaConsumer the Kafka consumer for answer responses
     */
    @Autowired
    public AnswerServiceImpl(
            AnswerRepository repository,
            @Lazy AnswerService self,
            AnswerKafkaProvider answerKafkaProvider,
            AnswerKafkaConsumer answerKafkaConsumer) {
        super(repository);
        this.self = self;
        this.answerKafkaProvider = answerKafkaProvider;
        this.answerKafkaConsumer = answerKafkaConsumer;
    }

    @Override
    public Answer save(Answer answer) {
        logger.debug("Saving answer and scheduling AI check");
        Answer saved = super.save(answer);
        logger.debug("Answer saved with ID: {}, scheduling async AI check", saved.getId());
        self.checkAnswerWithAiAsync(saved.getId());
        return saved;
    }

    @Override
    public List<Answer> findAnswersByUser(User user) {
        logger.debug("Finding answers for user ID: {}", user.getId());
        List<Answer> answers = repository.findByUser(user);
        logger.debug("Found {} answers for user ID: {}", answers.size(), user.getId());
        return answers;
    }

    @Override
    public Page<Answer> findAnswersByUser(User user, Pageable pageable) {
        logger.debug("Finding answers for user ID: {} with pagination", user.getId());
        Page<Answer> answerPage = repository.findByUser(user, pageable);
        logger.debug("Found {} answers for user ID: {} (page {} of {})",
                answerPage.getNumberOfElements(), user.getId(),
                pageable.getPageNumber(), answerPage.getTotalPages());
        return answerPage;
    }

    @Override
    public List<Answer> findAnswersByQuestion(InterviewQuestion question) {
        logger.debug("Finding answers for question ID: {}", question.getId());
        List<Answer> answers = repository.findByQuestion(question);
        logger.debug("Found {} answers for question ID: {}", answers.size(), question.getId());
        return answers;
    }

    @Override
    public Page<Answer> findAnswersByQuestion(InterviewQuestion question, Pageable pageable) {
        logger.debug("Finding answers for question ID: {} with pagination", question.getId());
        Page<Answer> answerPage = repository.findByQuestion(question, pageable);
        logger.debug("Found {} answers for question ID: {} (page {} of {})",
                answerPage.getNumberOfElements(), question.getId(),
                pageable.getPageNumber(), answerPage.getTotalPages());
        return answerPage;
    }

    @Override
    public List<Answer> findAnswersByUserAndQuestion(User user, InterviewQuestion question) {
        logger.debug("Finding answers for user ID: {} and question ID: {}", user.getId(), question.getId());
        List<Answer> answers = repository.findByUserAndQuestion(user, question);
        logger.debug("Found {} answers for user ID: {} and question ID: {}",
                answers.size(), user.getId(), question.getId());
        return answers;
    }

    @Override
    public Answer checkAnswerWithAi(UUID answerId) {
        logger.debug("Checking answer with AI for answer ID: {}", answerId);
        Answer answer = findAndValidateAnswer(answerId);

        try {
            Map<String, Object> cheatingResult = performCheatingCheck(answer);
            Boolean isCheating = (Boolean) cheatingResult.get("isCheating");
            updateAnswerWithCheatingResult(answer, isCheating);

            if (Boolean.TRUE.equals(isCheating)) {
                return handleCheatingDetected(answer);
            }

            return performRegularAnswerCheck(answer);
        } catch (Exception e) {
            logger.error("Error while checking answer with AI: {}", e.getMessage(), e);
            throw e;
        }
    }

    private Answer findAndValidateAnswer(UUID answerId) {
        return findById(answerId)
                .orElseThrow(() -> {
                    logger.error("Answer not found with ID: {}", answerId);
                    return new NoSuchElementException("Answer not found with ID: " + answerId);
                });
    }

    private Map<String, Object> performCheatingCheck(Answer answer) {
        String questionText = answer.getQuestion().getQuestionText();
        String answerText = answer.getText();
        logger.debug("Processing question text (length: {}) and answer text (length: {})",
                questionText.length(), answerText.length());

        String cheatingMessageId = java.util.UUID.randomUUID().toString();
        logger.debug("Checking for cheating before evaluating answer");

        CompletableFuture<Map<String, Object>> cheatingResponseFuture =
                answerKafkaConsumer.registerPendingRequest(cheatingMessageId);
        answerKafkaProvider.sendAnswerCheatingCheckRequest(cheatingMessageId, questionText, answerText);

        return cheatingResponseFuture.join();
    }

    private void updateAnswerWithCheatingResult(Answer answer, Boolean isCheating) {
        answer.setIsCheating(isCheating);
        logger.debug("Updated answer with cheating check result: isCheating={}", isCheating);
    }

    private Answer handleCheatingDetected(Answer answer) {
        logger.info("Cheating detected in answer ID: {}. Skipping regular answer check.", answer.getId());
        answer.setAiScore(0.0);
        answer.setAiFeedback("This answer appears to contain evidence of cheating. Please provide your own original answer.");
        return save(answer);
    }

    private Answer performRegularAnswerCheck(Answer answer) {
        logger.debug("No cheating detected. Proceeding with regular answer check.");
        String messageId = java.util.UUID.randomUUID().toString();

        CompletableFuture<Map<String, Object>> responseFuture =
                answerKafkaConsumer.registerPendingRequest(messageId);

        answerKafkaProvider.sendAnswerFeedbackRequest(messageId,
                answer.getQuestion().getQuestionText(),
                answer.getText());

        Map<String, Object> result = responseFuture.join();

        updateAnswerWithFeedback(answer, result);
        return save(answer);
    }

    private void updateAnswerWithFeedback(Answer answer, Map<String, Object> result) {
        Double score = (Double) result.get("score");
        String feedback = (String) result.get("feedback");
        logger.debug("Received AI feedback: score={}, feedback length={}", score, feedback.length());

        answer.setAiScore(score);
        answer.setAiFeedback(feedback);
        logger.debug("Updated answer with AI score and feedback");
    }

    @Override
    @Async
    public CompletableFuture<Answer> checkAnswerWithAiAsync(UUID answerId) {
        logger.debug("Starting async check of answer with AI for answer ID: {}", answerId);
        try {
            Answer answer = checkAnswerWithAi(answerId);
            logger.debug("Completed async check of answer with AI for answer ID: {}", answerId);
            return CompletableFuture.completedFuture(answer);
        } catch (Exception e) {
            logger.error("Error in async check of answer with AI: {}", e.getMessage(), e);
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

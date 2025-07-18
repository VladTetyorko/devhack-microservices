package com.vladte.devhack.common.service.domain;

import com.vladte.devhack.common.config.TestConfig;
import com.vladte.devhack.common.repository.AnswerRepository;
import com.vladte.devhack.common.service.BaseServiceTest;
import com.vladte.devhack.common.service.domain.impl.AnswerServiceImpl;
import com.vladte.devhack.common.service.kafka.concumers.AnswerKafkaConsumer;
import com.vladte.devhack.common.service.kafka.producers.impl.AnswerKafkaProviderImpl;
import com.vladte.devhack.entities.Answer;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.User;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the AnswerService implementation.
 */
@Epic("Service Layer")
@Feature("Answer Management")
@Import(TestConfig.class)
public class AnswerServiceTest extends BaseServiceTest {

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private AnswerKafkaProviderImpl answerKafkaProvider;

    @Mock
    private AnswerKafkaConsumer answerKafkaConsumer;

    private AnswerService answerService;
    private AnswerService selfReference;

    @BeforeEach
    public void setup() {
        // Create a self-reference for the service (used in constructor)
        selfReference = mock(AnswerService.class);

        // Initialize the service with mocked dependencies
        answerService = new AnswerServiceImpl(answerRepository, selfReference, answerKafkaProvider, answerKafkaConsumer);
    }

    @Test
    @DisplayName("Find answers by user should return list of answers")
    @Description("Test that findAnswersByUser returns the correct list of answers for a given user")
    @Severity(SeverityLevel.NORMAL)
    public void testFindAnswersByUser() {
        // Arrange
        User user = createTestUser();
        List<Answer> expectedAnswers = Arrays.asList(
                createTestAnswer(user, UUID.randomUUID()),
                createTestAnswer(user, UUID.randomUUID())
        );

        when(answerRepository.findByUser(user)).thenReturn(expectedAnswers);

        // Act
        List<Answer> actualAnswers = answerService.findAnswersByUser(user);

        // Assert
        assertEquals(expectedAnswers.size(), actualAnswers.size());
        assertEquals(expectedAnswers, actualAnswers);

        // Verify repository was called
        verify(answerRepository).findByUser(user);

        // Attach test data to Allure report
        attachJson("Test User", user.toString());
        attachJson("Expected Answers", expectedAnswers.toString());
    }

    @Test
    @DisplayName("Find answers by user with pagination should return page of answers")
    @Description("Test that findAnswersByUser with pagination returns the correct page of answers")
    @Severity(SeverityLevel.NORMAL)
    public void testFindAnswersByUserWithPagination() {
        // Arrange
        User user = createTestUser();
        Pageable pageable = mock(Pageable.class);
        List<Answer> answerList = Arrays.asList(
                createTestAnswer(user, UUID.randomUUID()),
                createTestAnswer(user, UUID.randomUUID())
        );
        Page<Answer> expectedPage = new PageImpl<>(answerList, pageable, answerList.size());

        when(answerRepository.findByUser(user, pageable)).thenReturn(expectedPage);

        // Act
        Page<Answer> actualPage = answerService.findAnswersByUser(user, pageable);

        // Assert
        assertEquals(expectedPage.getTotalElements(), actualPage.getTotalElements());
        assertEquals(expectedPage.getContent(), actualPage.getContent());

        // Verify repository was called
        verify(answerRepository).findByUser(user, pageable);
    }

    @Test
    @DisplayName("Find answers by question should return list of answers")
    @Description("Test that findAnswersByQuestion returns the correct list of answers for a given question")
    @Severity(SeverityLevel.NORMAL)
    public void testFindAnswersByQuestion() {
        // Arrange
        InterviewQuestion question = createTestQuestion();
        List<Answer> expectedAnswers = Arrays.asList(
                createTestAnswer(createTestUser(), UUID.randomUUID(), question),
                createTestAnswer(createTestUser(), UUID.randomUUID(), question)
        );

        when(answerRepository.findByQuestion(question)).thenReturn(expectedAnswers);

        // Act
        List<Answer> actualAnswers = answerService.findAnswersByQuestion(question);

        // Assert
        assertEquals(expectedAnswers.size(), actualAnswers.size());
        assertEquals(expectedAnswers, actualAnswers);

        // Verify repository was called
        verify(answerRepository).findByQuestion(question);
    }

    @Test
    @DisplayName("Check answer with AI should update answer with feedback")
    @Description("Test that checkAnswerWithAi correctly processes an answer and updates it with AI feedback")
    @Severity(SeverityLevel.CRITICAL)
    public void testCheckAnswerWithAi() {
        // Arrange
        UUID answerId = UUID.randomUUID();
        User user = createTestUser();
        InterviewQuestion question = createTestQuestion();
        Answer answer = createTestAnswer(user, answerId, question);

        // Mock the cheating check
        Map<String, Object> cheatingResult = new HashMap<>();
        cheatingResult.put("isCheating", false);

        // Mock the AI feedback
        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("score", 85.0);
        aiResult.put("feedback", "Good answer, but could be improved");

        // Setup mocks
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));

        // Mock the CompletableFuture for cheating check
        CompletableFuture<Map<String, Object>> cheatingFuture = CompletableFuture.completedFuture(cheatingResult);
        when(answerKafkaConsumer.registerPendingRequest(any())).thenReturn(cheatingFuture);

        // Mock the CompletableFuture for AI feedback
        CompletableFuture<Map<String, Object>> aiFuture = CompletableFuture.completedFuture(aiResult);
        when(answerKafkaConsumer.registerPendingRequest(any())).thenReturn(aiFuture);

        // Mock the Kafka provider methods
        when(answerKafkaProvider.subscribeToAnswerCheatingCheck(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        when(answerKafkaProvider.subscribeToAnswerFeedbackCheck(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        when(answerRepository.save(any(Answer.class))).thenReturn(answer);

        // Act
        Answer checkedAnswer = answerService.checkAnswerWithAi(answerId);

        // Assert
        assertNotNull(checkedAnswer);
        assertEquals(85.0, checkedAnswer.getAiScore());
        assertEquals("Good answer, but could be improved", checkedAnswer.getAiFeedback());

        // Verify repository and Kafka provider were called
        verify(answerRepository).findById(answerId);
        verify(answerKafkaProvider).subscribeToAnswerCheatingCheck(any(), any(), any());
        verify(answerKafkaProvider).subscribeToAnswerFeedbackCheck(any(), any(), any());
        verify(answerRepository).save(any(Answer.class));
    }

    @Test
    @DisplayName("Check answer with AI async should return completable future")
    @Description("Test that checkAnswerWithAiAsync returns a CompletableFuture that completes with the updated answer")
    @Severity(SeverityLevel.CRITICAL)
    public void testCheckAnswerWithAiAsync() {
        // Arrange
        UUID answerId = UUID.randomUUID();
        User user = createTestUser();
        InterviewQuestion question = createTestQuestion();
        Answer answer = createTestAnswer(user, answerId, question);

        // Mock the cheating check
        Map<String, Object> cheatingResult = new HashMap<>();
        cheatingResult.put("isCheating", false);

        // Mock the AI feedback
        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("score", 85.0);
        aiResult.put("feedback", "Good answer, but could be improved");

        // Setup mocks
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));

        // Mock the CompletableFuture for cheating check
        CompletableFuture<Map<String, Object>> cheatingFuture = CompletableFuture.completedFuture(cheatingResult);
        when(answerKafkaConsumer.registerPendingRequest(any())).thenReturn(cheatingFuture);

        // Mock the CompletableFuture for AI feedback
        CompletableFuture<Map<String, Object>> aiFuture = CompletableFuture.completedFuture(aiResult);
        when(answerKafkaConsumer.registerPendingRequest(any())).thenReturn(aiFuture);

        // Mock the Kafka provider methods
        when(answerKafkaProvider.subscribeToAnswerCheatingCheck(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        when(answerKafkaProvider.subscribeToAnswerFeedbackCheck(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        when(answerRepository.save(any(Answer.class))).thenReturn(answer);

        // Act
        CompletableFuture<Answer> future = answerService.checkAnswerWithAiAsync(answerId);

        // Assert
        assertNotNull(future);

        // Verify repository was called
        verify(answerRepository).findById(answerId);
    }

    // Helper methods to create test objects

    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password"); // Required field
        return user;
    }

    private InterviewQuestion createTestQuestion() {
        InterviewQuestion question = new InterviewQuestion();
        question.setId(UUID.randomUUID());
        question.setQuestionText("Test question?");
        question.setDifficulty("Medium");
        question.setUser(createTestUser()); // Required field
        return question;
    }

    private Answer createTestAnswer(User user, UUID id) {
        return createTestAnswer(user, id, createTestQuestion());
    }

    private Answer createTestAnswer(User user, UUID id, InterviewQuestion question) {
        Answer answer = new Answer();
        answer.setId(id);
        answer.setUser(user);
        answer.setQuestion(question);
        answer.setText("Test answer");
        answer.setCreatedAt(LocalDateTime.now());
        return answer;
    }
}

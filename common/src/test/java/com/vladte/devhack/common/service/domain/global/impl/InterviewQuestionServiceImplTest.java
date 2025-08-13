package com.vladte.devhack.common.service.domain.global.impl;

import com.vladte.devhack.common.repository.global.InterviewQuestionRepository;
import com.vladte.devhack.common.service.BaseServiceTest;
import com.vladte.devhack.common.service.domain.audit.AuditService;
import com.vladte.devhack.common.service.websocket.QuestionWebSocketService;
import com.vladte.devhack.entities.global.InterviewQuestion;
import com.vladte.devhack.entities.global.Tag;
import com.vladte.devhack.entities.user.User;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for InterviewQuestionServiceImpl.
 * Tests all interview question management functionality including search, filtering, and statistics.
 */
@DisplayName("Interview Question Service Implementation Tests")
class InterviewQuestionServiceImplTest extends BaseServiceTest {

    @Mock
    private InterviewQuestionRepository repository;

    @Mock
    private AuditService auditService;

    @Mock
    private QuestionWebSocketService webSocketService;

    private InterviewQuestionServiceImpl questionService;

    @BeforeEach
    void setUp() {
        questionService = new InterviewQuestionServiceImpl(repository, auditService, webSocketService);
    }

    @Test
    @DisplayName("Should find questions by tag")
    @Description("Test that findQuestionsByTag returns questions associated with a specific tag")
    @Severity(SeverityLevel.CRITICAL)
    void testFindQuestionsByTag() {
        // Given
        Tag javaTag = createTestTag("Java");
        InterviewQuestion question1 = createTestQuestion("What is polymorphism?", "Medium");
        InterviewQuestion question2 = createTestQuestion("Explain inheritance", "Easy");
        List<InterviewQuestion> expectedQuestions = List.of(question1, question2);

        when(repository.findByTagsContaining(javaTag)).thenReturn(expectedQuestions);

        // When
        List<InterviewQuestion> result = questionService.findQuestionsByTag(javaTag);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedQuestions, result);
        verify(repository).findByTagsContaining(javaTag);
    }

    @Test
    @DisplayName("Should find questions by tag with pagination")
    @Description("Test that findQuestionsByTag returns paginated questions associated with a specific tag")
    @Severity(SeverityLevel.CRITICAL)
    void testFindQuestionsByTagWithPagination() {
        // Given
        Tag springTag = createTestTag("Spring");
        InterviewQuestion question1 = createTestQuestion("What is Spring Boot?", "Medium");
        InterviewQuestion question2 = createTestQuestion("Explain dependency injection", "Hard");
        List<InterviewQuestion> questions = List.of(question1, question2);
        Page<InterviewQuestion> expectedPage = new PageImpl<>(questions);
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findByTagsContaining(springTag, pageable)).thenReturn(expectedPage);

        // When
        Page<InterviewQuestion> result = questionService.findQuestionsByTag(springTag, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(expectedPage, result);
        verify(repository).findByTagsContaining(springTag, pageable);
    }

    @Test
    @DisplayName("Should search questions with filters")
    @Description("Test that searchQuestions returns filtered questions based on query, difficulty, and tag")
    @Severity(SeverityLevel.CRITICAL)
    void testSearchQuestions() {
        // Given
        String query = "Java";
        String difficulty = "Medium";
        UUID tagId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        InterviewQuestion question1 = createTestQuestion("Java polymorphism question", "Medium");
        InterviewQuestion question2 = createTestQuestion("Java inheritance question", "Medium");
        List<InterviewQuestion> questions = List.of(question1, question2);
        Page<InterviewQuestion> expectedPage = new PageImpl<>(questions);

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expectedPage);

        // When
        Page<InterviewQuestion> result = questionService.searchQuestions(query, difficulty, tagId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(expectedPage, result);
        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should search questions with null filters")
    @Description("Test that searchQuestions handles null parameters correctly")
    @Severity(SeverityLevel.NORMAL)
    void testSearchQuestionsWithNullFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        InterviewQuestion question = createTestQuestion("General question", "Easy");
        Page<InterviewQuestion> expectedPage = new PageImpl<>(List.of(question));

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expectedPage);

        // When
        Page<InterviewQuestion> result = questionService.searchQuestions(null, null, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(expectedPage, result);
        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should count questions by user")
    @Description("Test that countQuestionsByUser returns correct count for a specific user")
    @Severity(SeverityLevel.NORMAL)
    void testCountQuestionsByUser() {
        // Given
        User user = createTestUser();
        UUID userId = user.getId();
        int expectedCount = 5;

        when(repository.countInterviewQuestionsByUserId(userId)).thenReturn(expectedCount);

        // When
        int result = questionService.countQuestionsByUser(user);

        // Then
        assertEquals(expectedCount, result);
        verify(repository).countInterviewQuestionsByUserId(userId);
    }

    @Test
    @DisplayName("Should return zero when counting questions for null user")
    @Description("Test that countQuestionsByUser returns 0 when user is null")
    @Severity(SeverityLevel.NORMAL)
    void testCountQuestionsByUserNull() {
        // When
        int result = questionService.countQuestionsByUser(null);

        // Then
        assertEquals(0, result);
        verify(repository, never()).countInterviewQuestionsByUserId(any());
    }

    @Test
    @DisplayName("Should count all questions")
    @Description("Test that countAllQuestions returns total count of all questions")
    @Severity(SeverityLevel.NORMAL)
    void testCountAllQuestions() {
        // Given
        InterviewQuestion question1 = createTestQuestion("Question 1", "Easy");
        InterviewQuestion question2 = createTestQuestion("Question 2", "Medium");
        InterviewQuestion question3 = createTestQuestion("Question 3", "Hard");
        List<InterviewQuestion> allQuestions = List.of(question1, question2, question3);

        when(repository.findAll()).thenReturn(allQuestions);

        // When
        int result = questionService.countAllQuestions();

        // Then
        assertEquals(3, result);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Should count answered questions by user")
    @Description("Test that findAnsweredQuestionsByUser returns correct count of answered questions")
    @Severity(SeverityLevel.NORMAL)
    void testFindAnsweredQuestionsByUser() {
        // Given
        User user = createTestUser();
        UUID userId = user.getId();
        int expectedCount = 3;

        when(repository.countQuestionsWithAnswerByUserId(userId)).thenReturn(expectedCount);

        // When
        int result = questionService.findAnsweredQuestionsByUser(user);

        // Then
        assertEquals(expectedCount, result);
        verify(repository).countQuestionsWithAnswerByUserId(userId);
    }

    @Test
    @DisplayName("Should return zero when counting answered questions for null user")
    @Description("Test that findAnsweredQuestionsByUser returns 0 when user is null")
    @Severity(SeverityLevel.NORMAL)
    void testFindAnsweredQuestionsByUserNull() {
        // When
        int result = questionService.findAnsweredQuestionsByUser(null);

        // Then
        assertEquals(0, result);
        verify(repository, never()).countQuestionsWithAnswerByUserId(any());
    }

    @Test
    @DisplayName("Should handle empty results when finding questions by tag")
    @Description("Test that findQuestionsByTag handles empty results gracefully")
    @Severity(SeverityLevel.NORMAL)
    void testFindQuestionsByTagEmpty() {
        // Given
        Tag emptyTag = createTestTag("EmptyTag");
        when(repository.findByTagsContaining(emptyTag)).thenReturn(List.of());

        // When
        List<InterviewQuestion> result = questionService.findQuestionsByTag(emptyTag);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findByTagsContaining(emptyTag);
    }

    @Test
    @DisplayName("Should handle empty search results")
    @Description("Test that searchQuestions handles empty search results gracefully")
    @Severity(SeverityLevel.NORMAL)
    void testSearchQuestionsEmpty() {
        // Given
        String query = "NonExistentTopic";
        String difficulty = "Expert";
        UUID tagId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<InterviewQuestion> emptyPage = new PageImpl<>(List.of());

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        // When
        Page<InterviewQuestion> result = questionService.searchQuestions(query, difficulty, tagId, pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * Helper method to create a test interview question.
     */
    private InterviewQuestion createTestQuestion(String questionText, String difficulty) {
        InterviewQuestion question = new InterviewQuestion();
        question.setId(UUID.randomUUID());
        question.setQuestionText(questionText);
        question.setDifficulty(difficulty);
        return question;
    }

    /**
     * Helper method to create a test tag.
     */
    private Tag createTestTag(String name) {
        Tag tag = new Tag();
        tag.setId(UUID.randomUUID());
        tag.setName(name);
        return tag;
    }

    /**
     * Helper method to create a test user.
     */
    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        return user;
    }
}
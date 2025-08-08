package com.vladte.devhack.common.service.domain.global.impl;

import com.vladte.devhack.common.repository.global.TagRepository;
import com.vladte.devhack.common.service.BaseServiceTest;
import com.vladte.devhack.common.service.domain.audit.AuditService;
import com.vladte.devhack.common.service.domain.personalized.AnswerService;
import com.vladte.devhack.entities.global.InterviewQuestion;
import com.vladte.devhack.entities.global.Tag;
import com.vladte.devhack.entities.personalized.Answer;
import com.vladte.devhack.entities.user.User;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for TagServiceImpl.
 * Tests all tag management functionality including progress calculation and statistics.
 */
@DisplayName("Tag Service Implementation Tests")
class TagServiceImplTest extends BaseServiceTest {

    @Mock
    private TagRepository repository;

    @Mock
    private AuditService auditService;

    @Mock
    private AnswerService answerService;

    private TagServiceImpl tagService;

    @BeforeEach
    void setUp() {
        tagService = new TagServiceImpl(repository, auditService, answerService);
    }

    @Test
    @DisplayName("Should find tag by name")
    @Description("Test that findTagByName returns tag when found")
    @Severity(SeverityLevel.CRITICAL)
    void testFindTagByName() {
        // Given
        String tagName = "Java";
        Tag expectedTag = createTestTag(tagName, "java");

        when(repository.findByName(tagName)).thenReturn(Optional.of(expectedTag));

        // When
        Optional<Tag> result = tagService.findTagByName(tagName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedTag, result.get());
        assertEquals(tagName, result.get().getName());
        verify(repository).findByName(tagName);
    }

    @Test
    @DisplayName("Should return empty when tag not found by name")
    @Description("Test that findTagByName returns empty when tag not found")
    @Severity(SeverityLevel.NORMAL)
    void testFindTagByNameNotFound() {
        // Given
        String tagName = "NonExistent";

        when(repository.findByName(tagName)).thenReturn(Optional.empty());

        // When
        Optional<Tag> result = tagService.findTagByName(tagName);

        // Then
        assertFalse(result.isPresent());
        verify(repository).findByName(tagName);
    }

    @Test
    @DisplayName("Should find tag by slug")
    @Description("Test that findTagBySlug returns tag when found")
    @Severity(SeverityLevel.CRITICAL)
    void testFindTagBySlug() {
        // Given
        String slug = "spring-boot";
        Tag expectedTag = createTestTag("Spring Boot", slug);
        List<Tag> allTags = List.of(
                createTestTag("Java", "java"),
                expectedTag,
                createTestTag("Python", "python")
        );

        when(repository.findAll()).thenReturn(allTags);

        // When
        Optional<Tag> result = tagService.findTagBySlug(slug);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedTag, result.get());
        assertEquals(slug, result.get().getSlug());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Should return empty when tag not found by slug")
    @Description("Test that findTagBySlug returns empty when tag not found")
    @Severity(SeverityLevel.NORMAL)
    void testFindTagBySlugNotFound() {
        // Given
        String slug = "non-existent";
        List<Tag> allTags = List.of(
                createTestTag("Java", "java"),
                createTestTag("Python", "python")
        );

        when(repository.findAll()).thenReturn(allTags);

        // When
        Optional<Tag> result = tagService.findTagBySlug(slug);

        // Then
        assertFalse(result.isPresent());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Should calculate progress for tag with answered questions")
    @Description("Test that calculateProgress correctly calculates progress when user has answered some questions")
    @Severity(SeverityLevel.CRITICAL)
    void testCalculateProgressWithAnsweredQuestions() {
        // Given
        User user = createTestUser();
        Tag tag = createTestTag("Java", "java");

        // Create questions for the tag
        InterviewQuestion question1 = createTestQuestion("Question 1");
        InterviewQuestion question2 = createTestQuestion("Question 2");
        InterviewQuestion question3 = createTestQuestion("Question 3");
        Set<InterviewQuestion> questions = Set.of(question1, question2, question3);
        tag.setQuestions(questions);

        // Create answers for some questions
        Answer answer1 = createTestAnswer(question1, user);
        Answer answer2 = createTestAnswer(question2, user);
        List<Answer> userAnswers = List.of(answer1, answer2);

        when(answerService.findAnswersByUser(user)).thenReturn(userAnswers);

        // When
        Tag result = tagService.calculateProgress(tag, user);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getAnsweredQuestions()); // 2 out of 3 questions answered
        verify(answerService).findAnswersByUser(user);
    }

    @Test
    @DisplayName("Should calculate zero progress when no questions answered")
    @Description("Test that calculateProgress returns zero progress when user hasn't answered any questions")
    @Severity(SeverityLevel.NORMAL)
    void testCalculateProgressWithNoAnsweredQuestions() {
        // Given
        User user = createTestUser();
        Tag tag = createTestTag("Python", "python");

        InterviewQuestion question1 = createTestQuestion("Python Question 1");
        InterviewQuestion question2 = createTestQuestion("Python Question 2");
        Set<InterviewQuestion> questions = Set.of(question1, question2);
        tag.setQuestions(questions);

        when(answerService.findAnswersByUser(user)).thenReturn(List.of());

        // When
        Tag result = tagService.calculateProgress(tag, user);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getAnsweredQuestions());
        assertEquals(0.0, result.getProgressPercentage());
        verify(answerService).findAnswersByUser(user);
    }

    @Test
    @DisplayName("Should handle null tag in calculateProgress")
    @Description("Test that calculateProgress handles null tag gracefully")
    @Severity(SeverityLevel.NORMAL)
    void testCalculateProgressWithNullTag() {
        // Given
        User user = createTestUser();

        // When
        Tag result = tagService.calculateProgress(null, user);

        // Then
        assertNull(result);
        verify(answerService, never()).findAnswersByUser(any());
    }

    @Test
    @DisplayName("Should handle null user in calculateProgress")
    @Description("Test that calculateProgress handles null user gracefully")
    @Severity(SeverityLevel.NORMAL)
    void testCalculateProgressWithNullUser() {
        // Given
        Tag tag = createTestTag("Java", "java");

        // When
        Tag result = tagService.calculateProgress(tag, null);

        // Then
        assertEquals(tag, result);
        verify(answerService, never()).findAnswersByUser(any());
    }

    @Test
    @DisplayName("Should handle tag with no questions")
    @Description("Test that calculateProgress handles tag with empty questions set")
    @Severity(SeverityLevel.NORMAL)
    void testCalculateProgressWithNoQuestions() {
        // Given
        User user = createTestUser();
        Tag tag = createTestTag("EmptyTag", "empty");
        tag.setQuestions(Set.of()); // Empty questions set

        // When
        Tag result = tagService.calculateProgress(tag, user);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getAnsweredQuestions());
        assertEquals(0.0, result.getProgressPercentage());
        verify(answerService, never()).findAnswersByUser(any());
    }

    @Test
    @DisplayName("Should calculate progress for all tags")
    @Description("Test that calculateProgressForAll calculates progress for multiple tags")
    @Severity(SeverityLevel.CRITICAL)
    void testCalculateProgressForAll() {
        // Given
        User user = createTestUser();
        Tag tag1 = createTestTag("Java", "java");
        Tag tag2 = createTestTag("Python", "python");
        List<Tag> tags = List.of(tag1, tag2);

        // Set up questions for tags
        InterviewQuestion question1 = createTestQuestion("Java Question");
        InterviewQuestion question2 = createTestQuestion("Python Question");
        tag1.setQuestions(Set.of(question1));
        tag2.setQuestions(Set.of(question2));

        // Set up answers
        Answer answer1 = createTestAnswer(question1, user);
        when(answerService.findAnswersByUser(user)).thenReturn(List.of(answer1));

        // When
        List<Tag> result = tagService.calculateProgressForAll(tags, user);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(answerService, times(2)).findAnswersByUser(user);
    }

    @Test
    @DisplayName("Should return empty list when calculating progress for null tags")
    @Description("Test that calculateProgressForAll handles null tags list")
    @Severity(SeverityLevel.NORMAL)
    void testCalculateProgressForAllWithNullTags() {
        // Given
        User user = createTestUser();

        // When
        List<Tag> result = tagService.calculateProgressForAll(null, user);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(answerService, never()).findAnswersByUser(any());
    }

    @Test
    @DisplayName("Should return empty list when calculating progress for null user")
    @Description("Test that calculateProgressForAll handles null user")
    @Severity(SeverityLevel.NORMAL)
    void testCalculateProgressForAllWithNullUser() {
        // Given
        List<Tag> tags = List.of(createTestTag("Java", "java"));

        // When
        List<Tag> result = tagService.calculateProgressForAll(tags, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(answerService, never()).findAnswersByUser(any());
    }

    @Test
    @DisplayName("Should count tags by user")
    @Description("Test that countTagsByUser returns correct count of tags used by user")
    @Severity(SeverityLevel.NORMAL)
    void testCountTagsByUser() {
        // Given
        User user = createTestUser();
        UUID userId = user.getId();

        // Create questions with user
        InterviewQuestion userQuestion1 = createTestQuestion("User Question 1");
        userQuestion1.setUser(user);
        InterviewQuestion userQuestion2 = createTestQuestion("User Question 2");
        userQuestion2.setUser(user);
        InterviewQuestion otherQuestion = createTestQuestion("Other Question");
        otherQuestion.setUser(createTestUser()); // Different user

        // Create tags with questions
        Tag tag1 = createTestTag("Java", "java");
        tag1.setQuestions(Set.of(userQuestion1));
        Tag tag2 = createTestTag("Python", "python");
        tag2.setQuestions(Set.of(userQuestion2));
        Tag tag3 = createTestTag("JavaScript", "javascript");
        tag3.setQuestions(Set.of(otherQuestion)); // Not user's question

        List<Tag> allTags = List.of(tag1, tag2, tag3);
        when(repository.findAll()).thenReturn(allTags);

        // When
        int result = tagService.countTagsByUser(user);

        // Then
        assertEquals(2, result); // Only tag1 and tag2 should be counted
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Should return zero when counting tags for null user")
    @Description("Test that countTagsByUser returns 0 when user is null")
    @Severity(SeverityLevel.NORMAL)
    void testCountTagsByUserNull() {
        // When
        int result = tagService.countTagsByUser(null);

        // Then
        assertEquals(0, result);
        verify(repository, never()).findAll();
    }

    @Test
    @DisplayName("Should count all tags")
    @Description("Test that countAllTags returns total count of all tags")
    @Severity(SeverityLevel.NORMAL)
    void testCountAllTags() {
        // Given
        List<Tag> allTags = List.of(
                createTestTag("Java", "java"),
                createTestTag("Python", "python"),
                createTestTag("JavaScript", "javascript")
        );
        when(repository.findAll()).thenReturn(allTags);

        // When
        int result = tagService.countAllTags();

        // Then
        assertEquals(3, result);
        verify(repository).findAll();
    }

    /**
     * Helper method to create a test tag.
     */
    private Tag createTestTag(String name, String slug) {
        Tag tag = new Tag();
        tag.setId(UUID.randomUUID());
        tag.setName(name);
        tag.setQuestions(new HashSet<>());
        return tag;
    }

    /**
     * Helper method to create a test interview question.
     */
    private InterviewQuestion createTestQuestion(String questionText) {
        InterviewQuestion question = new InterviewQuestion();
        question.setId(UUID.randomUUID());
        question.setQuestionText(questionText);
        return question;
    }

    /**
     * Helper method to create a test user.
     */
    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        return user;
    }

    /**
     * Helper method to create a test answer.
     */
    private Answer createTestAnswer(InterviewQuestion question, User user) {
        Answer answer = new Answer();
        answer.setId(UUID.randomUUID());
        answer.setQuestion(question);
        answer.setUser(user);
        answer.setText("Test answer");
        return answer;
    }
}

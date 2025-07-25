package com.vladte.devhack.common.repository;

import com.vladte.devhack.common.repository.global.InterviewQuestionRepository;
import com.vladte.devhack.common.repository.global.TagRepository;
import com.vladte.devhack.common.repository.user.UserRepository;
import com.vladte.devhack.entities.enums.AuthProviderType;
import com.vladte.devhack.entities.global.InterviewQuestion;
import com.vladte.devhack.entities.global.Tag;
import com.vladte.devhack.entities.user.AuthenticationProvider;
import com.vladte.devhack.entities.user.Profile;
import com.vladte.devhack.entities.user.User;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the InterviewQuestionRepository.
 */
@DisplayName("Interview Question Repository Tests")
class InterviewQuestionRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private InterviewQuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    private User testUser;
    private Tag testTag;

    @BeforeEach
    void setup() {
        // Create and save a test user
        testUser = new User();

        // Create profile
        Profile profile = new Profile();
        profile.setName("testuser");
        profile.setUser(testUser);
        testUser.setProfile(profile);

        // Create authentication provider
        AuthenticationProvider authProvider = new AuthenticationProvider();
        authProvider.setProvider(AuthProviderType.LOCAL);
        authProvider.setEmail("test@example.com");
        authProvider.setPasswordHash("password");
        authProvider.setUser(testUser);
        testUser.setAuthProviders(List.of(authProvider));

        userRepository.save(testUser);

        // Create and save a test tag
        testTag = new Tag();
        testTag.setName("Java");
        tagRepository.save(testTag);

        // Clear any existing questions
        questionRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save a question")
    @Description("Test that a question can be saved to the database")
    @Severity(SeverityLevel.BLOCKER)
    void testSaveQuestion() {
        // Arrange
        InterviewQuestion question = createTestQuestion("What is polymorphism?");

        // Act
        InterviewQuestion savedQuestion = questionRepository.save(question);

        // Assert
        assertNotNull(savedQuestion.getId());
        assertNotNull(savedQuestion.getCreatedAt());
        assertEquals("What is polymorphism?", savedQuestion.getQuestionText());
        assertEquals("Medium", savedQuestion.getDifficulty());
        assertEquals(testUser.getId(), savedQuestion.getUser().getId());
    }

    @Test
    @DisplayName("Should find a question by ID")
    @Description("Test that a question can be retrieved by its ID")
    @Severity(SeverityLevel.CRITICAL)
    void testFindQuestionById() {
        // Arrange
        InterviewQuestion question = createTestQuestion("What is inheritance?");
        InterviewQuestion savedQuestion = questionRepository.save(question);
        UUID questionId = savedQuestion.getId();

        // Act
        Optional<InterviewQuestion> foundQuestion = questionRepository.findById(questionId);

        // Assert
        assertTrue(foundQuestion.isPresent());
        assertEquals(questionId, foundQuestion.get().getId());
        assertEquals("What is inheritance?", foundQuestion.get().getQuestionText());
    }

    @Test
    @DisplayName("Should update a question")
    @Description("Test that a question can be updated in the database")
    @Severity(SeverityLevel.CRITICAL)
    void testUpdateQuestion() {
        // Arrange
        InterviewQuestion question = createTestQuestion("Original question?");
        InterviewQuestion savedQuestion = questionRepository.save(question);
        UUID questionId = savedQuestion.getId();

        // Act
        savedQuestion.setQuestionText("Updated question?");
        savedQuestion.setDifficulty("Hard");
        InterviewQuestion updatedQuestion = questionRepository.save(savedQuestion);

        // Assert
        assertEquals(questionId, updatedQuestion.getId());
        assertEquals("Updated question?", updatedQuestion.getQuestionText());
        assertEquals("Hard", updatedQuestion.getDifficulty());
    }

    @Test
    @DisplayName("Should delete a question")
    @Description("Test that a question can be deleted from the database")
    @Severity(SeverityLevel.CRITICAL)
    void testDeleteQuestion() {
        // Arrange
        InterviewQuestion question = createTestQuestion("Test question?");
        InterviewQuestion savedQuestion = questionRepository.save(question);
        UUID questionId = savedQuestion.getId();

        // Act
        questionRepository.deleteById(questionId);

        // Assert
        Optional<InterviewQuestion> foundQuestion = questionRepository.findById(questionId);
        assertFalse(foundQuestion.isPresent());
    }

    @Test
    @DisplayName("Should find questions by difficulty")
    @Description("Test that questions can be found by difficulty")
    @Severity(SeverityLevel.CRITICAL)
    void testFindByDifficulty() {
        // Arrange
        InterviewQuestion question1 = createTestQuestion("Easy question?");
        question1.setDifficulty("Easy");
        InterviewQuestion question2 = createTestQuestion("Medium question?");
        question2.setDifficulty("Medium");
        InterviewQuestion question3 = createTestQuestion("Hard question?");
        question3.setDifficulty("Hard");

        questionRepository.save(question1);
        questionRepository.save(question2);
        questionRepository.save(question3);

        // Act
        List<InterviewQuestion> easyQuestions = questionRepository.findByDifficulty("Easy");
        List<InterviewQuestion> mediumQuestions = questionRepository.findByDifficulty("Medium");
        List<InterviewQuestion> hardQuestions = questionRepository.findByDifficulty("Hard");

        // Assert
        assertEquals(1, easyQuestions.size());
        assertEquals("Easy question?", easyQuestions.get(0).getQuestionText());

        assertEquals(1, mediumQuestions.size());
        assertEquals("Medium question?", mediumQuestions.get(0).getQuestionText());

        assertEquals(1, hardQuestions.size());
        assertEquals("Hard question?", hardQuestions.get(0).getQuestionText());
    }

    @Test
    @DisplayName("Should find questions by tag")
    @Description("Test that questions can be found by tag")
    @Severity(SeverityLevel.CRITICAL)
    void testFindByTagsContaining() {
        // Arrange
        InterviewQuestion question1 = createTestQuestion("Java question?");
        question1.getTags().add(testTag);

        InterviewQuestion question2 = createTestQuestion("Another Java question?");
        question2.getTags().add(testTag);

        InterviewQuestion question3 = createTestQuestion("Non-tagged question?");

        questionRepository.save(question1);
        questionRepository.save(question2);
        questionRepository.save(question3);

        // Act
        List<InterviewQuestion> javaQuestions = questionRepository.findByTagsContaining(testTag);

        // Assert
        assertEquals(2, javaQuestions.size());
        assertTrue(javaQuestions.stream().anyMatch(q -> q.getQuestionText().equals("Java question?")));
        assertTrue(javaQuestions.stream().anyMatch(q -> q.getQuestionText().equals("Another Java question?")));
    }

    @Test
    @DisplayName("Should find questions by difficulty and tag")
    @Description("Test that questions can be found by both difficulty and tag")
    @Severity(SeverityLevel.CRITICAL)
    void testFindByDifficultyAndTagsContaining() {
        // Arrange
        InterviewQuestion question1 = createTestQuestion("Easy Java question?");
        question1.setDifficulty("Easy");
        question1.getTags().add(testTag);

        InterviewQuestion question2 = createTestQuestion("Medium Java question?");
        question2.setDifficulty("Medium");
        question2.getTags().add(testTag);

        InterviewQuestion question3 = createTestQuestion("Hard Java question?");
        question3.setDifficulty("Hard");
        question3.getTags().add(testTag);

        questionRepository.save(question1);
        questionRepository.save(question2);
        questionRepository.save(question3);

        // Act
        List<InterviewQuestion> mediumJavaQuestions = questionRepository.findByDifficultyAndTagsContaining("Medium", testTag);

        // Assert
        assertEquals(1, mediumJavaQuestions.size());
        assertEquals("Medium Java question?", mediumJavaQuestions.get(0).getQuestionText());
    }

    @Test
    @DisplayName("Should find questions by difficulty with pagination")
    @Description("Test that questions can be found by difficulty with pagination")
    @Severity(SeverityLevel.CRITICAL)
    void testFindByDifficultyPaginated() {
        // Arrange
        for (int i = 1; i <= 10; i++) {
            InterviewQuestion question = createTestQuestion("Medium question " + i + "?");
            question.setDifficulty("Medium");
            questionRepository.save(question);
        }

        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());

        // Act
        Page<InterviewQuestion> questionPage = questionRepository.findByDifficulty("Medium", pageable);

        // Assert
        assertEquals(5, questionPage.getContent().size());
        assertEquals(10, questionPage.getTotalElements());
        assertEquals(2, questionPage.getTotalPages());
    }

    @Test
    @DisplayName("Should search questions with filters")
    @Description("Test that questions can be searched with text, difficulty, and tag ID filters")
    @Severity(SeverityLevel.CRITICAL)
    void testSearchQuestions() {
        // Arrange
        InterviewQuestion question1 = createTestQuestion("Java programming question?");
        question1.setDifficulty("Easy");
        question1.getTags().add(testTag);

        InterviewQuestion question2 = createTestQuestion("Python programming question?");
        question2.setDifficulty("Medium");

        InterviewQuestion question3 = createTestQuestion("JavaScript programming question?");
        question3.setDifficulty("Hard");
        question3.getTags().add(testTag);

        questionRepository.save(question1);
        questionRepository.save(question2);
        questionRepository.save(question3);

        Pageable pageable = PageRequest.of(0, 10);

        // Act - Search by text
        Page<InterviewQuestion> javaQuestions = questionRepository.searchQuestions("Java", null, null, pageable);

        // Assert
        assertEquals(2, javaQuestions.getContent().size());
        assertTrue(javaQuestions.getContent().stream().anyMatch(q -> q.getQuestionText().equals("Java programming question?")));
        assertTrue(javaQuestions.getContent().stream().anyMatch(q -> q.getQuestionText().equals("JavaScript programming question?")));

        // Act - Search by difficulty
        Page<InterviewQuestion> easyQuestions = questionRepository.searchQuestions(null, "Easy", null, pageable);

        // Assert
        assertEquals(1, easyQuestions.getContent().size());
        assertEquals("Java programming question?", easyQuestions.getContent().get(0).getQuestionText());

        // Act - Search by tag ID
        Page<InterviewQuestion> taggedQuestions = questionRepository.searchQuestions(null, null, testTag.getId(), pageable);

        // Assert
        assertEquals(2, taggedQuestions.getContent().size());

        // Act - Search with all filters
        Page<InterviewQuestion> filteredQuestions = questionRepository.searchQuestions(
                "Java", "Easy", testTag.getId(), pageable);

        // Assert
        assertEquals(1, filteredQuestions.getContent().size());
        assertEquals("Java programming question?", filteredQuestions.getContent().get(0).getQuestionText());
    }

    /**
     * Helper method to create a test question.
     */
    private InterviewQuestion createTestQuestion(String questionText) {
        InterviewQuestion question = new InterviewQuestion();
        question.setQuestionText(questionText);
        question.setDifficulty("Medium");
        question.setUser(testUser);
        return question;
    }
}

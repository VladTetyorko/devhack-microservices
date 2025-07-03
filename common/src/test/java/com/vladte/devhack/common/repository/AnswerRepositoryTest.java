package com.vladte.devhack.common.repository;

import com.vladte.devhack.entities.Answer;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.User;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the AnswerRepository.
 */
@DisplayName("Answer Repository Tests")
class AnswerRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterviewQuestionRepository questionRepository;

    private User testUser;
    private InterviewQuestion testQuestion;

    @BeforeEach
    void setup() {
        // Create and save a test user
        testUser = new User();
        testUser.setName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        userRepository.save(testUser);

        // Create and save a test question
        testQuestion = new InterviewQuestion();
        testQuestion.setQuestionText("Test question?");
        testQuestion.setDifficulty("Medium");
        // Set required user for question
        testQuestion.setUser(testUser);
        questionRepository.save(testQuestion);

        // Clear any existing answers
        answerRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save an answer")
    @Description("Test that an answer can be saved to the database")
    @Severity(SeverityLevel.BLOCKER)
    void testSaveAnswer() {
        // Arrange
        Answer answer = createTestAnswer("Test answer");

        // Act
        Answer savedAnswer = answerRepository.save(answer);

        // Assert
        assertNotNull(savedAnswer.getId());
        assertNotNull(savedAnswer.getCreatedAt());
        assertEquals("Test answer", savedAnswer.getText());
        assertEquals(testUser.getId(), savedAnswer.getUser().getId());
        assertEquals(testQuestion.getId(), savedAnswer.getQuestion().getId());
    }

    @Test
    @DisplayName("Should find an answer by ID")
    @Description("Test that an answer can be retrieved by its ID")
    @Severity(SeverityLevel.CRITICAL)
    void testFindAnswerById() {
        // Arrange
        Answer answer = createTestAnswer("Test answer");
        Answer savedAnswer = answerRepository.save(answer);
        UUID answerId = savedAnswer.getId();

        // Act
        Optional<Answer> foundAnswer = answerRepository.findById(answerId);

        // Assert
        assertTrue(foundAnswer.isPresent());
        assertEquals(answerId, foundAnswer.get().getId());
        assertEquals("Test answer", foundAnswer.get().getText());
    }

    @Test
    @DisplayName("Should update an answer")
    @Description("Test that an answer can be updated in the database")
    @Severity(SeverityLevel.CRITICAL)
    void testUpdateAnswer() {
        // Arrange
        Answer answer = createTestAnswer("Original answer");
        Answer savedAnswer = answerRepository.save(answer);
        UUID answerId = savedAnswer.getId();

        // Act
        savedAnswer.setText("Updated answer");
        savedAnswer.setAiScore(85.5);
        savedAnswer.setAiFeedback("Good answer");
        Answer updatedAnswer = answerRepository.save(savedAnswer);

        // Assert
        assertEquals(answerId, updatedAnswer.getId());
        assertEquals("Updated answer", updatedAnswer.getText());
        assertEquals(85.5, updatedAnswer.getAiScore());
        assertEquals("Good answer", updatedAnswer.getAiFeedback());
    }

    @Test
    @DisplayName("Should delete an answer")
    @Description("Test that an answer can be deleted from the database")
    @Severity(SeverityLevel.CRITICAL)
    void testDeleteAnswer() {
        // Arrange
        Answer answer = createTestAnswer("Test answer");
        Answer savedAnswer = answerRepository.save(answer);
        UUID answerId = savedAnswer.getId();

        // Act
        answerRepository.deleteById(answerId);

        // Assert
        Optional<Answer> foundAnswer = answerRepository.findById(answerId);
        assertFalse(foundAnswer.isPresent());
    }

    @Test
    @DisplayName("Should find answers by user")
    @Description("Test that answers can be found by user")
    @Severity(SeverityLevel.CRITICAL)
    void testFindByUser() {
        // Arrange
        Answer answer1 = createTestAnswer("Answer 1");
        Answer answer2 = createTestAnswer("Answer 2");
        answerRepository.save(answer1);
        answerRepository.save(answer2);

        // Act
        List<Answer> answers = answerRepository.findByUser(testUser);

        // Assert
        assertEquals(2, answers.size());
        assertTrue(answers.stream().anyMatch(a -> a.getText().equals("Answer 1")));
        assertTrue(answers.stream().anyMatch(a -> a.getText().equals("Answer 2")));
    }

    @Test
    @DisplayName("Should find answers by question")
    @Description("Test that answers can be found by question")
    @Severity(SeverityLevel.CRITICAL)
    void testFindByQuestion() {
        // Arrange
        Answer answer1 = createTestAnswer("Answer 1");
        Answer answer2 = createTestAnswer("Answer 2");
        answerRepository.save(answer1);
        answerRepository.save(answer2);

        // Act
        List<Answer> answers = answerRepository.findByQuestion(testQuestion);

        // Assert
        assertEquals(2, answers.size());
        assertTrue(answers.stream().anyMatch(a -> a.getText().equals("Answer 1")));
        assertTrue(answers.stream().anyMatch(a -> a.getText().equals("Answer 2")));
    }

    @Test
    @DisplayName("Should find answers by user and question")
    @Description("Test that answers can be found by both user and question")
    @Severity(SeverityLevel.CRITICAL)
    void testFindByUserAndQuestion() {
        // Arrange
        Answer answer1 = createTestAnswer("Answer 1");
        Answer answer2 = createTestAnswer("Answer 2");
        answerRepository.save(answer1);
        answerRepository.save(answer2);

        // Create another user and question
        User anotherUser = new User();
        anotherUser.setName("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password");
        userRepository.save(anotherUser);

        InterviewQuestion anotherQuestion = new InterviewQuestion();
        anotherQuestion.setQuestionText("Another question?");
        anotherQuestion.setDifficulty("Easy");
        anotherQuestion.setUser(anotherUser);
        questionRepository.save(anotherQuestion);

        // Create answers with different user/question combinations
        Answer answer3 = new Answer();
        answer3.setText("Answer 3");
        answer3.setUser(anotherUser);
        answer3.setQuestion(testQuestion);
        answerRepository.save(answer3);

        Answer answer4 = new Answer();
        answer4.setText("Answer 4");
        answer4.setUser(testUser);
        answer4.setQuestion(anotherQuestion);
        answerRepository.save(answer4);

        // Act
        List<Answer> answers = answerRepository.findByUserAndQuestion(testUser, testQuestion);

        // Assert
        assertEquals(2, answers.size());
        assertTrue(answers.stream().anyMatch(a -> a.getText().equals("Answer 1")));
        assertTrue(answers.stream().anyMatch(a -> a.getText().equals("Answer 2")));
    }

    @Test
    @DisplayName("Should find answers by user with pagination")
    @Description("Test that answers can be found by user with pagination")
    @Severity(SeverityLevel.CRITICAL)
    void testFindByUserPaginated() {
        // Arrange
        for (int i = 1; i <= 10; i++) {
            Answer answer = createTestAnswer("Answer " + i);
            answerRepository.save(answer);
        }

        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());

        // Act
        Page<Answer> answerPage = answerRepository.findByUser(testUser, pageable);

        // Assert
        assertEquals(5, answerPage.getContent().size());
        assertEquals(10, answerPage.getTotalElements());
        assertEquals(2, answerPage.getTotalPages());
    }

    @Test
    @DisplayName("Should search answers with filters")
    @Description("Test that answers can be searched with text, user ID, and question ID filters")
    @Severity(SeverityLevel.CRITICAL)
    void testSearchAnswers() {
        // Arrange
        Answer answer1 = createTestAnswer("Java programming answer");
        Answer answer2 = createTestAnswer("Python programming answer");
        Answer answer3 = createTestAnswer("JavaScript programming answer");
        answerRepository.save(answer1);
        answerRepository.save(answer2);
        answerRepository.save(answer3);

        Pageable pageable = PageRequest.of(0, 10);

        // Act - Search by text
        Page<Answer> javaAnswers = answerRepository.searchAnswers("Java", null, null, pageable);

        // Assert
        assertEquals(2, javaAnswers.getContent().size());
        assertTrue(javaAnswers.getContent().stream().anyMatch(a -> a.getText().equals("Java programming answer")));
        assertTrue(javaAnswers.getContent().stream().anyMatch(a -> a.getText().equals("JavaScript programming answer")));

        // Act - Search by user ID
        Page<Answer> userAnswers = answerRepository.searchAnswers(null, testUser.getId(), null, pageable);

        // Assert
        assertEquals(3, userAnswers.getContent().size());

        // Act - Search by question ID
        Page<Answer> questionAnswers = answerRepository.searchAnswers(null, null, testQuestion.getId(), pageable);

        // Assert
        assertEquals(3, questionAnswers.getContent().size());

        // Act - Search with all filters
        Page<Answer> filteredAnswers = answerRepository.searchAnswers(
                "Java", testUser.getId(), testQuestion.getId(), pageable);

        // Assert
        assertEquals(2, filteredAnswers.getContent().size());
    }

    /**
     * Helper method to create a test answer.
     */
    private Answer createTestAnswer(String text) {
        Answer answer = new Answer();
        answer.setText(text);
        answer.setUser(testUser);
        answer.setQuestion(testQuestion);
        answer.setCreatedAt(LocalDateTime.now());
        return answer;
    }
}

package com.vladte.devhack.common.mapper;

import com.vladte.devhack.common.model.mapper.AnswerMapper;
import com.vladte.devhack.common.model.mapper.dto.AnswerDTO;
import com.vladte.devhack.entities.Answer;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.User;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Answer Mapper Tests")
class AnswerMapperTest {

    private AnswerMapper answerMapper;
    private Answer answer;
    private AnswerDTO answerDTO;
    private User user;
    private InterviewQuestion question;
    private final UUID USER_ID = UUID.randomUUID();
    private final UUID QUESTION_ID = UUID.randomUUID();
    private final UUID ANSWER_ID = UUID.randomUUID();
    private final LocalDateTime NOW = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        answerMapper = new AnswerMapper();

        // Create test user
        user = new User();
        user.setId(USER_ID);
        user.setName("Test User");

        // Create test question
        question = new InterviewQuestion();
        question.setId(QUESTION_ID);
        question.setQuestionText("Test Question");

        // Create test answer entity
        answer = new Answer();
        answer.setId(ANSWER_ID);
        answer.setText("Test Answer");
        answer.setConfidenceLevel(80);
        answer.setAiScore(0.85);
        answer.setAiFeedback("Good answer");
        answer.setIsCorrect(true);
        answer.setIsCheating(false);
        answer.setUpdatedAt(NOW);
        answer.setCreatedAt(NOW);
        answer.setUser(user);
        answer.setQuestion(question);

        // Create test answer DTO
        answerDTO = new AnswerDTO();
        answerDTO.setId(ANSWER_ID);
        answerDTO.setText("Test Answer");
        answerDTO.setConfidenceLevel(80);
        answerDTO.setAiScore(0.85);
        answerDTO.setAiFeedback("Good answer");
        answerDTO.setIsCorrect(true);
        answerDTO.setIsCheating(false);
        answerDTO.setUpdatedAt(NOW);
        answerDTO.setCreatedAt(NOW);
        answerDTO.setUserId(USER_ID);
        answerDTO.setUserName("Test User");
        answerDTO.setQuestionId(QUESTION_ID);
        answerDTO.setQuestionText("Test Question");
    }

    @Test
    @DisplayName("Should convert entity to DTO with valid entity")
    @Description("Test that an Answer entity can be correctly converted to an AnswerDTO")
    @Severity(SeverityLevel.CRITICAL)
    void testToDTOWithValidEntity() {
        AnswerDTO result = answerMapper.toDTO(answer);

        assertNotNull(result);
        assertEquals(ANSWER_ID, result.getId());
        assertEquals("Test Answer", result.getText());
        assertEquals(80, result.getConfidenceLevel());
        assertEquals(0.85, result.getAiScore());
        assertEquals("Good answer", result.getAiFeedback());
        assertTrue(result.getIsCorrect());
        // isCheating is not mapped in the AnswerMapper.toDTO method
        assertNull(result.getIsCheating());
        assertEquals(NOW, result.getUpdatedAt());
        assertEquals(NOW, result.getCreatedAt());
        assertEquals(USER_ID, result.getUserId());
        assertEquals("Test User", result.getUserName());
        assertEquals(QUESTION_ID, result.getQuestionId());
        assertEquals("Test Question", result.getQuestionText());
    }

    @Test
    @DisplayName("Should handle null entity when converting to DTO")
    @Description("Test that the mapper returns null when a null entity is provided")
    @Severity(SeverityLevel.NORMAL)
    void testToDTOWithNullEntity() {
        AnswerDTO result = answerMapper.toDTO(null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null relationships when converting to DTO")
    @Description("Test that the mapper correctly handles null user and question relationships")
    @Severity(SeverityLevel.NORMAL)
    void testToDTOWithNullRelationships() {
        answer.setUser(null);
        answer.setQuestion(null);

        AnswerDTO result = answerMapper.toDTO(answer);

        assertNotNull(result);
        assertEquals(ANSWER_ID, result.getId());
        assertNull(result.getUserId());
        assertNull(result.getUserName());
        assertNull(result.getQuestionId());
        assertNull(result.getQuestionText());
    }

    @Test
    @DisplayName("Should convert DTO to entity with valid DTO")
    @Description("Test that an AnswerDTO can be correctly converted to an Answer entity")
    @Severity(SeverityLevel.CRITICAL)
    void testToEntityWithValidDTO() {
        Answer result = answerMapper.toEntity(answerDTO);

        assertNotNull(result);
        assertEquals(ANSWER_ID, result.getId());
        assertEquals("Test Answer", result.getText());
        assertEquals(80, result.getConfidenceLevel());
        assertEquals(0.85, result.getAiScore());
        assertEquals("Good answer", result.getAiFeedback());
        assertTrue(result.getIsCorrect());
        // isCheating is not mapped in the AnswerMapper.toEntity method
        assertNull(result.getIsCheating());
        assertEquals(NOW, result.getUpdatedAt());

        // User and question should be null as they are set by the service layer
        assertNull(result.getUser());
        assertNull(result.getQuestion());
    }

    @Test
    @DisplayName("Should handle null DTO when converting to entity")
    @Description("Test that the mapper returns null when a null DTO is provided")
    @Severity(SeverityLevel.NORMAL)
    void testToEntityWithNullDTO() {
        Answer result = answerMapper.toEntity(null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should update entity from DTO")
    @Description("Test that an existing Answer entity can be correctly updated from an AnswerDTO")
    @Severity(SeverityLevel.CRITICAL)
    void testUpdateEntityFromDTO() {
        // Create a new entity with different values
        Answer entityToUpdate = new Answer();
        entityToUpdate.setId(ANSWER_ID);
        entityToUpdate.setText("Old Answer");
        entityToUpdate.setConfidenceLevel(50);
        entityToUpdate.setAiScore(0.5);
        entityToUpdate.setAiFeedback("Old feedback");
        entityToUpdate.setIsCorrect(false);
        entityToUpdate.setIsCheating(true);

        // Update the entity with DTO values
        Answer result = answerMapper.updateEntityFromDTO(entityToUpdate, answerDTO);

        // Verify the entity was updated
        assertNotNull(result);
        assertEquals(ANSWER_ID, result.getId());
        assertEquals("Test Answer", result.getText());
        assertEquals(80, result.getConfidenceLevel());
        assertEquals(0.85, result.getAiScore());
        assertEquals("Good answer", result.getAiFeedback());
        assertTrue(result.getIsCorrect());
        // isCheating is not updated in the AnswerMapper.updateEntityFromDTO method
        assertTrue(result.getIsCheating());

        // User and question should not be updated by the mapper
        assertNull(result.getUser());
        assertNull(result.getQuestion());
    }

    @Test
    @DisplayName("Should handle null entity when updating from DTO")
    @Description("Test that the mapper returns null when a null entity is provided for updating")
    @Severity(SeverityLevel.NORMAL)
    void testUpdateEntityFromDTOWithNullEntity() {
        Answer result = answerMapper.updateEntityFromDTO(null, answerDTO);
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null DTO when updating entity")
    @Description("Test that the mapper returns the original entity when a null DTO is provided for updating")
    @Severity(SeverityLevel.NORMAL)
    void testUpdateEntityFromDTOWithNullDTO() {
        Answer result = answerMapper.updateEntityFromDTO(answer, null);
        assertSame(answer, result);
    }

    @Test
    @DisplayName("Should convert list of entities to list of DTOs")
    @Description("Test that a list of Answer entities can be correctly converted to a list of AnswerDTOs")
    @Severity(SeverityLevel.CRITICAL)
    void testToDTOList() {
        List<Answer> answers = Arrays.asList(answer, answer);
        List<AnswerDTO> result = answerMapper.toDTOList(answers);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(ANSWER_ID, result.get(0).getId());
        assertEquals(ANSWER_ID, result.get(1).getId());
    }

    @Test
    @DisplayName("Should convert list of DTOs to list of entities")
    @Description("Test that a list of AnswerDTOs can be correctly converted to a list of Answer entities")
    @Severity(SeverityLevel.CRITICAL)
    void testToEntityList() {
        List<AnswerDTO> dtos = Arrays.asList(answerDTO, answerDTO);
        List<Answer> result = answerMapper.toEntityList(dtos);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(ANSWER_ID, result.get(0).getId());
        assertEquals(ANSWER_ID, result.get(1).getId());
    }
}

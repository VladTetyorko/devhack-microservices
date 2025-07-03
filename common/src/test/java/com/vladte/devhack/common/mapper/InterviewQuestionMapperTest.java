package com.vladte.devhack.common.mapper;

import com.vladte.devhack.common.dto.InterviewQuestionDTO;
import com.vladte.devhack.entities.*;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Interview Question Mapper Tests")
class InterviewQuestionMapperTest {

    private InterviewQuestionMapper questionMapper;
    private InterviewQuestion question;
    private InterviewQuestionDTO questionDTO;
    private User user;
    private Set<Tag> tags;
    private List<Answer> answers;
    private List<Note> notes;
    private final UUID USER_ID = UUID.randomUUID();
    private final UUID QUESTION_ID = UUID.randomUUID();
    private final UUID TAG_ID_1 = UUID.randomUUID();
    private final UUID TAG_ID_2 = UUID.randomUUID();
    private final UUID ANSWER_ID_1 = UUID.randomUUID();
    private final UUID ANSWER_ID_2 = UUID.randomUUID();
    private final UUID NOTE_ID_1 = UUID.randomUUID();
    private final UUID NOTE_ID_2 = UUID.randomUUID();
    private final LocalDateTime NOW = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        questionMapper = new InterviewQuestionMapper();

        // Create test user
        user = new User();
        user.setId(USER_ID);
        user.setName("Test User");

        // Create test tags
        Tag tag1 = new Tag();
        tag1.setId(TAG_ID_1);
        tag1.setName("Java");

        Tag tag2 = new Tag();
        tag2.setId(TAG_ID_2);
        tag2.setName("Spring");

        tags = new HashSet<>(Arrays.asList(tag1, tag2));

        // Create test answers
        Answer answer1 = new Answer();
        answer1.setId(ANSWER_ID_1);
        answer1.setText("Test Answer 1");

        Answer answer2 = new Answer();
        answer2.setId(ANSWER_ID_2);
        answer2.setText("Test Answer 2");

        answers = new ArrayList<>(Arrays.asList(answer1, answer2));

        // Create test notes
        Note note1 = new Note();
        note1.setId(NOTE_ID_1);
        note1.setNoteText("Test Note 1");

        Note note2 = new Note();
        note2.setId(NOTE_ID_2);
        note2.setNoteText("Test Note 2");

        notes = new ArrayList<>(Arrays.asList(note1, note2));

        // Create test question entity
        question = new InterviewQuestion();
        question.setId(QUESTION_ID);
        question.setQuestionText("What is Spring Boot?");
        question.setDifficulty("Medium");
        question.setSource("Interview");
        question.setCreatedAt(NOW);
        question.setUser(user);
        question.setTags(tags);
        question.setAnswers(answers);
        question.setNotes(notes);

        // Create test question DTO
        questionDTO = new InterviewQuestionDTO();
        questionDTO.setId(QUESTION_ID);
        questionDTO.setQuestionText("What is Spring Boot?");
        questionDTO.setDifficulty("Medium");
        questionDTO.setSource("Interview");
        questionDTO.setCreatedAt(NOW);
        questionDTO.setUserId(USER_ID);
        questionDTO.setUserName("Test User");
        questionDTO.setTagIds(new HashSet<>(Arrays.asList(TAG_ID_1, TAG_ID_2)));
        questionDTO.setTagNames(new HashSet<>(Arrays.asList("Java", "Spring")));
        questionDTO.setAnswerIds(new ArrayList<>(Arrays.asList(ANSWER_ID_1, ANSWER_ID_2)));
        questionDTO.setNoteIds(new ArrayList<>(Arrays.asList(NOTE_ID_1, NOTE_ID_2)));
    }

    @Test
    @DisplayName("Should convert entity to DTO with valid entity")
    @Description("Test that an InterviewQuestion entity can be correctly converted to an InterviewQuestionDTO")
    @Severity(SeverityLevel.CRITICAL)
    void testToDTOWithValidEntity() {
        InterviewQuestionDTO result = questionMapper.toDTO(question);

        assertNotNull(result);
        assertEquals(QUESTION_ID, result.getId());
        assertEquals("What is Spring Boot?", result.getQuestionText());
        assertEquals("Medium", result.getDifficulty());
        assertEquals("Interview", result.getSource());
        assertEquals(NOW, result.getCreatedAt());
        assertEquals(USER_ID, result.getUserId());
        assertEquals("Test User", result.getUserName());

        // Check that tag IDs and names were mapped correctly
        assertEquals(2, result.getTagIds().size());
        assertTrue(result.getTagIds().contains(TAG_ID_1));
        assertTrue(result.getTagIds().contains(TAG_ID_2));
        assertEquals(2, result.getTagNames().size());
        assertTrue(result.getTagNames().contains("Java"));
        assertTrue(result.getTagNames().contains("Spring"));

        // Check that answer IDs were mapped correctly
        assertEquals(2, result.getAnswerIds().size());
        assertTrue(result.getAnswerIds().contains(ANSWER_ID_1));
        assertTrue(result.getAnswerIds().contains(ANSWER_ID_2));

        // Check that note IDs were mapped correctly
        assertEquals(2, result.getNoteIds().size());
        assertTrue(result.getNoteIds().contains(NOTE_ID_1));
        assertTrue(result.getNoteIds().contains(NOTE_ID_2));
    }

    @Test
    @DisplayName("Should handle null entity when converting to DTO")
    @Description("Test that the mapper returns null when a null entity is provided")
    @Severity(SeverityLevel.NORMAL)
    void testToDTOWithNullEntity() {
        InterviewQuestionDTO result = questionMapper.toDTO(null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null relationships when converting to DTO")
    @Description("Test that the mapper correctly handles null user, tags, answers, and notes relationships")
    @Severity(SeverityLevel.NORMAL)
    void testToDTOWithNullRelationships() {
        question.setUser(null);
        question.setTags(null);
        question.setAnswers(null);
        question.setNotes(null);

        InterviewQuestionDTO result = questionMapper.toDTO(question);

        assertNotNull(result);
        assertEquals(QUESTION_ID, result.getId());
        assertNull(result.getUserId());
        assertNull(result.getUserName());
        assertNotNull(result.getTagIds());
        assertTrue(result.getTagIds().isEmpty());
        assertNotNull(result.getTagNames());
        assertTrue(result.getTagNames().isEmpty());
        assertNotNull(result.getAnswerIds());
        assertTrue(result.getAnswerIds().isEmpty());
        assertNotNull(result.getNoteIds());
        assertTrue(result.getNoteIds().isEmpty());
    }

    @Test
    @DisplayName("Should convert DTO to entity with valid DTO")
    @Description("Test that an InterviewQuestionDTO can be correctly converted to an InterviewQuestion entity")
    @Severity(SeverityLevel.CRITICAL)
    void testToEntityWithValidDTO() {
        InterviewQuestion result = questionMapper.toEntity(questionDTO);

        assertNotNull(result);
        assertEquals(QUESTION_ID, result.getId());
        assertEquals("What is Spring Boot?", result.getQuestionText());
        assertEquals("Medium", result.getDifficulty());
        assertEquals("Interview", result.getSource());

        // User, tags, answers, and notes should be null as they are set by the service layer
        assertNull(result.getUser());
        assertNotNull(result.getTags());
        assertTrue(result.getTags().isEmpty());
        assertNotNull(result.getAnswers());
        assertTrue(result.getAnswers().isEmpty());
        assertNotNull(result.getNotes());
        assertTrue(result.getNotes().isEmpty());
    }

    @Test
    @DisplayName("Should handle null DTO when converting to entity")
    @Description("Test that the mapper returns null when a null DTO is provided")
    @Severity(SeverityLevel.NORMAL)
    void testToEntityWithNullDTO() {
        InterviewQuestion result = questionMapper.toEntity(null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should update entity from DTO")
    @Description("Test that an existing InterviewQuestion entity can be correctly updated from an InterviewQuestionDTO")
    @Severity(SeverityLevel.CRITICAL)
    void testUpdateEntityFromDTO() {
        // Create a new entity with different values
        InterviewQuestion entityToUpdate = new InterviewQuestion();
        entityToUpdate.setId(QUESTION_ID);
        entityToUpdate.setQuestionText("Old question text");
        entityToUpdate.setDifficulty("Easy");
        entityToUpdate.setSource("Old source");

        // Update the entity with DTO values
        InterviewQuestion result = questionMapper.updateEntityFromDTO(entityToUpdate, questionDTO);

        // Verify the entity was updated
        assertNotNull(result);
        assertEquals(QUESTION_ID, result.getId());
        assertEquals("What is Spring Boot?", result.getQuestionText());
        assertEquals("Medium", result.getDifficulty());
        assertEquals("Interview", result.getSource());

        // User, tags, answers, and notes should not be updated by the mapper
        assertNull(result.getUser());
        assertNotNull(result.getTags());
        assertTrue(result.getTags().isEmpty());
        assertNotNull(result.getAnswers());
        assertTrue(result.getAnswers().isEmpty());
        assertNotNull(result.getNotes());
        assertTrue(result.getNotes().isEmpty());
    }

    @Test
    @DisplayName("Should handle null entity when updating from DTO")
    @Description("Test that the mapper returns null when a null entity is provided for updating")
    @Severity(SeverityLevel.NORMAL)
    void testUpdateEntityFromDTOWithNullEntity() {
        InterviewQuestion result = questionMapper.updateEntityFromDTO(null, questionDTO);
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null DTO when updating entity")
    @Description("Test that the mapper returns the original entity when a null DTO is provided for updating")
    @Severity(SeverityLevel.NORMAL)
    void testUpdateEntityFromDTOWithNullDTO() {
        InterviewQuestion result = questionMapper.updateEntityFromDTO(question, null);
        assertSame(question, result);
    }

    @Test
    @DisplayName("Should convert list of entities to list of DTOs")
    @Description("Test that a list of InterviewQuestion entities can be correctly converted to a list of InterviewQuestionDTOs")
    @Severity(SeverityLevel.CRITICAL)
    void testToDTOList() {
        List<InterviewQuestion> questions = Arrays.asList(question, question);
        List<InterviewQuestionDTO> result = questionMapper.toDTOList(questions);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(QUESTION_ID, result.get(0).getId());
        assertEquals(QUESTION_ID, result.get(1).getId());
    }

    @Test
    @DisplayName("Should convert list of DTOs to list of entities")
    @Description("Test that a list of InterviewQuestionDTOs can be correctly converted to a list of InterviewQuestion entities")
    @Severity(SeverityLevel.CRITICAL)
    void testToEntityList() {
        List<InterviewQuestionDTO> dtos = Arrays.asList(questionDTO, questionDTO);
        List<InterviewQuestion> result = questionMapper.toEntityList(dtos);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(QUESTION_ID, result.get(0).getId());
        assertEquals(QUESTION_ID, result.get(1).getId());
    }
}

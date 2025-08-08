package com.vladte.devhack.common.mapper;

import com.vladte.devhack.common.model.dto.global.TagDTO;
import com.vladte.devhack.common.model.mapper.global.TagMapper;
import com.vladte.devhack.entities.global.InterviewQuestion;
import com.vladte.devhack.entities.global.Tag;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tag Mapper Tests")
class TagMapperTest {

    private TagMapper tagMapper;
    private Tag tag;
    private TagDTO tagDTO;
    private Set<InterviewQuestion> questions;
    private final UUID TAG_ID = UUID.randomUUID();
    private final UUID QUESTION_ID_1 = UUID.randomUUID();
    private final UUID QUESTION_ID_2 = UUID.randomUUID();
    private final LocalDateTime NOW = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        tagMapper = new TagMapper();

        // Create test questions
        InterviewQuestion question1 = new InterviewQuestion();
        question1.setId(QUESTION_ID_1);
        question1.setQuestionText("Test Question 1");

        InterviewQuestion question2 = new InterviewQuestion();
        question2.setId(QUESTION_ID_2);
        question2.setQuestionText("Test Question 2");

        questions = new HashSet<>(Arrays.asList(question1, question2));

        // Create test tag entity
        tag = new Tag();
        tag.setId(TAG_ID);
        tag.setName("Java");
        tag.setQuestions(questions);
        tag.setAnsweredQuestions(1);
        tag.setProgressPercentage(50.0);
        tag.setCreatedAt(NOW);

        // Create test tag DTO
        tagDTO = new TagDTO();
        tagDTO.setId(TAG_ID);
        tagDTO.setName("Java");
        tagDTO.setQuestionIds(new HashSet<>(Arrays.asList(QUESTION_ID_1, QUESTION_ID_2)));
        tagDTO.setAnsweredQuestions(1);
        tagDTO.setProgressPercentage(50.0);
        tagDTO.setCreatedAt(NOW);
    }

    @Test
    @DisplayName("Should convert entity to DTO with valid entity")
    @Description("Test that a Tag entity can be correctly converted to a TagDTO")
    @Severity(SeverityLevel.CRITICAL)
    void testToDTOWithValidEntity() {
        TagDTO result = tagMapper.toDTO(tag);

        assertNotNull(result);
        assertEquals(TAG_ID, result.getId());
        assertEquals("Java", result.getName());
        assertEquals(1, result.getAnsweredQuestions());
        assertEquals(50.0, result.getProgressPercentage());
        assertEquals(NOW, result.getCreatedAt());

        // Check that question IDs were mapped correctly
        assertEquals(2, result.getQuestionIds().size());
        assertTrue(result.getQuestionIds().contains(QUESTION_ID_1));
        assertTrue(result.getQuestionIds().contains(QUESTION_ID_2));
    }

    @Test
    @DisplayName("Should handle null entity when converting to DTO")
    @Description("Test that the mapper returns null when a null entity is provided")
    @Severity(SeverityLevel.NORMAL)
    void testToDTOWithNullEntity() {
        TagDTO result = tagMapper.toDTO(null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null questions when converting to DTO")
    @Description("Test that the mapper correctly handles null questions relationship")
    @Severity(SeverityLevel.NORMAL)
    void testToDTOWithNullQuestions() {
        tag.setQuestions(null);

        TagDTO result = tagMapper.toDTO(tag);

        assertNotNull(result);
        assertEquals(TAG_ID, result.getId());
        assertEquals("Java", result.getName());
        assertNotNull(result.getQuestionIds());
        assertTrue(result.getQuestionIds().isEmpty());
    }

    @Test
    @DisplayName("Should convert DTO to entity with valid DTO")
    @Description("Test that a TagDTO can be correctly converted to a Tag entity")
    @Severity(SeverityLevel.CRITICAL)
    void testToEntityWithValidDTO() {
        Tag result = tagMapper.toEntity(tagDTO);

        assertNotNull(result);
        assertEquals(TAG_ID, result.getId());
        assertEquals("Java", result.getName());
        assertEquals(1, result.getAnsweredQuestions());

        // Questions should be null as they are set by the service layer
        assertNotNull(result.getQuestions());
        assertTrue(result.getQuestions().isEmpty());
    }

    @Test
    @DisplayName("Should handle null DTO when converting to entity")
    @Description("Test that the mapper returns null when a null DTO is provided")
    @Severity(SeverityLevel.NORMAL)
    void testToEntityWithNullDTO() {
        Tag result = tagMapper.toEntity(null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should update entity from DTO")
    @Description("Test that an existing Tag entity can be correctly updated from a TagDTO")
    @Severity(SeverityLevel.CRITICAL)
    void testUpdateEntityFromDTO() {
        // Create a new entity with different values
        Tag entityToUpdate = new Tag();
        entityToUpdate.setId(TAG_ID);
        entityToUpdate.setName("Old Tag");
        entityToUpdate.setAnsweredQuestions(0);

        // Update the entity with DTO values
        Tag result = tagMapper.updateEntityFromDTO(entityToUpdate, tagDTO);

        // Verify the entity was updated
        assertNotNull(result);
        assertEquals(TAG_ID, result.getId());
        assertEquals("Java", result.getName());
        assertEquals(1, result.getAnsweredQuestions());

        // Questions should not be updated by the mapper
        assertNotNull(result.getQuestions());
        assertTrue(result.getQuestions().isEmpty());
    }

    @Test
    @DisplayName("Should handle null entity when updating from DTO")
    @Description("Test that the mapper returns null when a null entity is provided for updating")
    @Severity(SeverityLevel.NORMAL)
    void testUpdateEntityFromDTOWithNullEntity() {
        Tag result = tagMapper.updateEntityFromDTO(null, tagDTO);
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null DTO when updating entity")
    @Description("Test that the mapper returns the original entity when a null DTO is provided for updating")
    @Severity(SeverityLevel.NORMAL)
    void testUpdateEntityFromDTOWithNullDTO() {
        Tag result = tagMapper.updateEntityFromDTO(tag, null);
        assertSame(tag, result);
    }

    @Test
    @DisplayName("Should convert list of entities to list of DTOs")
    @Description("Test that a list of Tag entities can be correctly converted to a list of TagDTOs")
    @Severity(SeverityLevel.CRITICAL)
    void testToDTOList() {
        List<Tag> tags = Arrays.asList(tag, tag);
        List<TagDTO> result = tagMapper.toDTOList(tags);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(TAG_ID, result.get(0).getId());
        assertEquals(TAG_ID, result.get(1).getId());
    }

    @Test
    @DisplayName("Should convert list of DTOs to list of entities")
    @Description("Test that a list of TagDTOs can be correctly converted to a list of Tag entities")
    @Severity(SeverityLevel.CRITICAL)
    void testToEntityList() {
        List<TagDTO> dtos = Arrays.asList(tagDTO, tagDTO);
        List<Tag> result = tagMapper.toEntityList(dtos);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(TAG_ID, result.get(0).getId());
        assertEquals(TAG_ID, result.get(1).getId());
    }

    @Test
    @DisplayName("Should generate correct slugs")
    @Description("Test that slugs are correctly generated from tag names")
    @Severity(SeverityLevel.NORMAL)
    void testSlugGeneration() {
        tagDTO.setName("Java Programming");
        assertEquals("java-programming", tagDTO.getSlug());

        tag.setName("Java Programming");
        assertEquals("java-programming", tag.getSlug());
    }
}

package com.vladte.devhack.common.service;

import com.vladte.devhack.common.exception.ServiceException;
import com.vladte.devhack.common.exception.ValidationException;
import com.vladte.devhack.common.repository.global.TagRepository;
import com.vladte.devhack.common.service.domain.audit.AuditService;
import com.vladte.devhack.common.service.domain.global.impl.TagServiceImpl;
import com.vladte.devhack.common.service.domain.personalized.AnswerService;
import com.vladte.devhack.entities.global.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for TagService hierarchical functionality.
 */
@ExtendWith(MockitoExtension.class)
class TagServiceHierarchyTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private AnswerService answerService;

    private TagServiceImpl tagService;

    private Tag rootTag;
    private Tag childTag;
    private Tag grandChildTag;

    @BeforeEach
    void setUp() {
        tagService = new TagServiceImpl(tagRepository, auditService, answerService);

        // Create test hierarchy
        rootTag = createTag("Programming", "programming", "programming", null);
        childTag = createTag("Java", "java", "programming.java", rootTag);
        grandChildTag = createTag("Spring Boot", "spring_boot", "programming.java.spring_boot", childTag);
    }

    private Tag createTag(String name, String slug, String path, Tag parent) {
        Tag tag = new Tag();
        tag.setId(UUID.randomUUID());
        tag.setName(name);
        tag.setSlug(slug);
        tag.setPath(path);
        tag.setParent(parent);
        return tag;
    }

    @Test
    void testCreateTag_Success() {
        // Arrange
        when(tagRepository.findByParentAndSlug(null, "programming")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag tag = invocation.getArgument(0);
            tag.setId(UUID.randomUUID());
            return tag;
        });

        // Act
        Tag result = tagService.createTag("Programming", null);

        // Assert
        assertNotNull(result);
        assertEquals("Programming", result.getName());
        assertEquals("programming", result.getSlug());
        assertEquals("programming", result.getPath());
        assertNull(result.getParent());

        verify(tagRepository).findByParentAndSlug(null, "programming");
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void testCreateTag_WithParent() {
        // Arrange
        when(tagRepository.findByParentAndSlug(rootTag, "java")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag tag = invocation.getArgument(0);
            tag.setId(UUID.randomUUID());
            return tag;
        });

        // Act
        Tag result = tagService.createTag("Java", rootTag);

        // Assert
        assertNotNull(result);
        assertEquals("Java", result.getName());
        assertEquals("java", result.getSlug());
        assertEquals("programming.java", result.getPath());
        assertEquals(rootTag, result.getParent());

        verify(tagRepository).findByParentAndSlug(rootTag, "java");
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void testCreateTag_DuplicateSlug() {
        // Arrange
        when(tagRepository.findByParentAndSlug(rootTag, "java")).thenReturn(Optional.of(childTag));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class,
                () -> tagService.createTag("Java", rootTag));

        assertTrue(exception.getMessage().contains("already exists under the same parent"));
        verify(tagRepository).findByParentAndSlug(rootTag, "java");
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void testCreateTag_EmptyName() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> tagService.createTag("", null));
        assertThrows(ValidationException.class, () -> tagService.createTag(null, null));
    }

    @Test
    void testMoveTag_Success() {
        // Arrange
        Tag newParent = createTag("Backend", "backend", "backend", null);
        when(tagRepository.findByParentAndSlug(newParent, "java")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Tag result = tagService.moveTag(childTag, newParent);

        // Assert
        assertEquals(newParent, result.getParent());
        assertEquals("backend.java", result.getPath());

        verify(tagRepository).updateSubtreePaths("programming.java", "backend.java");
        verify(tagRepository).save(childTag);
    }

    @Test
    void testMoveTag_ToRoot() {
        // Arrange
        when(tagRepository.findByParentAndSlug(null, "java")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Tag result = tagService.moveTag(childTag, null);

        // Assert
        assertNull(result.getParent());
        assertEquals("java", result.getPath());

        verify(tagRepository).updateSubtreePaths("programming.java", "java");
        verify(tagRepository).save(childTag);
    }

    @Test
    void testMoveTag_CycleDetection() {
        // Arrange - try to move root under its child
        childTag.setPath("programming.java"); // Ensure path is set for cycle detection

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class,
                () -> tagService.moveTag(rootTag, childTag));

        assertTrue(exception.getMessage().contains("would create a cycle"));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void testMoveTag_ToSelf() {
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class,
                () -> tagService.moveTag(childTag, childTag));

        assertTrue(exception.getMessage().contains("would create a cycle"));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void testDeleteTag_Cascade() {
        // Act
        boolean result = tagService.deleteTag(childTag, true);

        // Assert
        assertTrue(result);
        verify(tagRepository).delete(childTag);
        verify(tagRepository, never()).findByParent(any());
    }

    @Test
    void testDeleteTag_OrphanChildren() {
        // Arrange
        when(tagRepository.findByParent(childTag)).thenReturn(Collections.singletonList(grandChildTag));
        when(tagRepository.findByParentAndSlug(null, "spring_boot")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        boolean result = tagService.deleteTag(childTag, false);

        // Assert
        assertTrue(result);
        verify(tagRepository).findByParent(childTag);
        verify(tagRepository).save(grandChildTag); // Should be moved to root
        verify(tagRepository).delete(childTag);
    }

    @Test
    void testFindRootTags() {
        // Arrange
        when(tagRepository.findByParentIsNull()).thenReturn(Collections.singletonList(rootTag));

        // Act
        List<Tag> result = tagService.findRootTags();

        // Assert
        assertEquals(1, result.size());
        assertEquals(rootTag, result.get(0));
        verify(tagRepository).findByParentIsNull();
    }

    @Test
    void testFindChildren() {
        // Arrange
        when(tagRepository.findByParent(rootTag)).thenReturn(Collections.singletonList(childTag));

        // Act
        List<Tag> result = tagService.findChildren(rootTag);

        // Assert
        assertEquals(1, result.size());
        assertEquals(childTag, result.get(0));
        verify(tagRepository).findByParent(rootTag);
    }

    @Test
    void testFindDescendants() {
        // Arrange
        when(tagRepository.findDescendants("programming")).thenReturn(Arrays.asList(rootTag, childTag, grandChildTag));

        // Act
        List<Tag> result = tagService.findDescendants(rootTag);

        // Assert
        assertEquals(3, result.size());
        verify(tagRepository).findDescendants("programming");
    }

    @Test
    void testFindDescendants_NullPath() {
        // Arrange
        rootTag.setPath(null);

        // Act
        List<Tag> result = tagService.findDescendants(rootTag);

        // Assert
        assertTrue(result.isEmpty());
        verify(tagRepository, never()).findDescendants(any());
    }

    @Test
    void testFindAncestors() {
        // Arrange
        when(tagRepository.findAncestors("programming.java.spring_boot"))
                .thenReturn(Arrays.asList(rootTag, childTag, grandChildTag));

        // Act
        List<Tag> result = tagService.findAncestors(grandChildTag);

        // Assert
        assertEquals(3, result.size());
        verify(tagRepository).findAncestors("programming.java.spring_boot");
    }

    @Test
    void testFindSubtree() {
        // Arrange
        when(tagRepository.findSubtreeWithDepth("programming", 2))
                .thenReturn(Arrays.asList(rootTag, childTag));

        // Act
        List<Tag> result = tagService.findSubtree(rootTag, 2);

        // Assert
        assertEquals(2, result.size());
        verify(tagRepository).findSubtreeWithDepth("programming", 2);
    }

    @Test
    void testFindSiblings() {
        // Arrange
        Tag sibling = createTag("Python", "python", "programming.python", rootTag);
        when(tagRepository.findSiblings(rootTag, childTag.getId())).thenReturn(List.of(sibling));

        // Act
        List<Tag> result = tagService.findSiblings(childTag);

        // Assert
        assertEquals(1, result.size());
        assertEquals(sibling, result.get(0));
        verify(tagRepository).findSiblings(rootTag, childTag.getId());
    }

    @Test
    void testFindTagsByDepth() {
        // Arrange
        when(tagRepository.findByDepth(1)).thenReturn(Collections.singletonList(rootTag));

        // Act
        List<Tag> result = tagService.findTagsByDepth(1);

        // Assert
        assertEquals(1, result.size());
        assertEquals(rootTag, result.get(0));
        verify(tagRepository).findByDepth(1);
    }

    @Test
    void testValidateMove_Valid() {
        // Arrange
        Tag newParent = createTag("Backend", "backend", "backend", null);
        when(tagRepository.findByParentAndSlug(newParent, "java")).thenReturn(Optional.empty());

        // Act
        boolean result = tagService.validateMove(childTag, newParent);

        // Assert
        assertTrue(result);
        verify(tagRepository).findByParentAndSlug(newParent, "java");
    }

    @Test
    void testValidateMove_SelfMove() {
        // Act
        boolean result = tagService.validateMove(childTag, childTag);

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateMove_CycleDetection() {
        // Act
        boolean result = tagService.validateMove(rootTag, childTag);

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateMove_DuplicateSlug() {
        // Arrange
        Tag existingTag = createTag("Java Existing", "java", "programming.java", rootTag);
        when(tagRepository.findByParentAndSlug(rootTag, "java")).thenReturn(Optional.of(existingTag));

        // Act
        boolean result = tagService.validateMove(childTag, rootTag);

        // Assert
        assertFalse(result);
        verify(tagRepository).findByParentAndSlug(rootTag, "java");
    }

    @Test
    void testFindTagBySlug() {
        // Arrange
        when(tagRepository.findBySlug("java")).thenReturn(Optional.of(childTag));

        // Act
        Optional<Tag> result = tagService.findTagBySlug("java");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(childTag, result.get());
        verify(tagRepository).findBySlug("java");
    }

    @Test
    void testFindTagBySlug_NotFound() {
        // Arrange
        when(tagRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<Tag> result = tagService.findTagBySlug("nonexistent");

        // Assert
        assertFalse(result.isPresent());
        verify(tagRepository).findBySlug("nonexistent");
    }

    @Test
    void testFindTagBySlug_EmptySlug() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> tagService.findTagBySlug(""));
        assertThrows(ValidationException.class, () -> tagService.findTagBySlug(null));
    }

    @Test
    void testServiceExceptionHandling() {
        // Arrange
        when(tagRepository.findByParentIsNull()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class,
                () -> tagService.findRootTags());

        assertTrue(exception.getMessage().contains("Failed to find root tags"));
        assertEquals("TAG_FIND_ROOTS_ERROR", exception.getErrorCode());
    }
}
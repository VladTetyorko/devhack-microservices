package com.vladte.devhack.common.entity;

import com.vladte.devhack.entities.global.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Tag entity hierarchical functionality.
 */
class TagHierarchyTest {

    private Tag rootTag;
    private Tag childTag;
    private Tag grandChildTag;

    @BeforeEach
    void setUp() {
        // Create a simple hierarchy: root -> child -> grandchild
        rootTag = new Tag();
        rootTag.setName("Programming");
        rootTag.setSlug("programming");
        rootTag.setPath("programming");

        childTag = new Tag();
        childTag.setName("Java");
        childTag.setSlug("java");
        childTag.setPath("programming.java");
        childTag.setParent(rootTag);

        grandChildTag = new Tag();
        grandChildTag.setName("Spring Boot");
        grandChildTag.setSlug("spring_boot");
        grandChildTag.setPath("programming.java.spring_boot");
        grandChildTag.setParent(childTag);
    }

    @Test
    void testGenerateSlug() {
        Tag tag = new Tag();

        // Test normal name
        tag.setName("Java Programming");
        assertEquals("java_programming", tag.generateSlug());

        // Test name starting with digit
        tag.setName("3D Graphics");
        assertEquals("t_3d_graphics", tag.generateSlug());

        // Test name with special characters
        tag.setName("C++ & STL");
        assertEquals("c_stl", tag.generateSlug());

        // Test empty name
        tag.setName("");
        assertEquals("", tag.generateSlug());

        // Test null name
        tag.setName(null);
        assertEquals("", tag.generateSlug());
    }

    @Test
    void testGetSlug() {
        Tag tag = new Tag();
        tag.setName("Java Programming");

        // Should generate slug if not set
        assertEquals("java_programming", tag.getSlug());

        // Should return existing slug if set
        tag.setSlug("custom_slug");
        assertEquals("custom_slug", tag.getSlug());
    }

    @Test
    void testGeneratePath() {
        // Test root tag path generation
        Tag root = new Tag();
        root.setName("Programming");
        assertEquals("programming", root.generatePath());

        // Test child tag path generation
        Tag child = new Tag();
        child.setName("Java");
        child.setParent(rootTag);
        assertEquals("programming.java", child.generatePath());

        // Test grandchild tag path generation
        Tag grandchild = new Tag();
        grandchild.setName("Spring Boot");
        grandchild.setParent(childTag);
        assertEquals("programming.java.spring_boot", grandchild.generatePath());
    }

    @Test
    void testGetDepth() {
        // Test depth calculation from path
        assertEquals(0, rootTag.getDepth());
        assertEquals(1, childTag.getDepth());
        assertEquals(2, grandChildTag.getDepth());

        // Test depth calculation without path (fallback to parent traversal)
        Tag tagWithoutPath = new Tag();
        tagWithoutPath.setParent(childTag);
        assertEquals(2, tagWithoutPath.getDepth());
    }

    @Test
    void testIsRoot() {
        assertTrue(rootTag.isRoot());
        assertFalse(childTag.isRoot());
        assertFalse(grandChildTag.isRoot());
    }

    @Test
    void testIsLeaf() {
        // Assuming no children are set in the test setup
        assertTrue(rootTag.isLeaf());
        assertTrue(childTag.isLeaf());
        assertTrue(grandChildTag.isLeaf());
    }

    @Test
    void testGetRoot() {
        assertEquals(rootTag, rootTag.getRoot());
        assertEquals(rootTag, childTag.getRoot());
        assertEquals(rootTag, grandChildTag.getRoot());
    }

    @Test
    void testIsAncestorOf() {
        assertTrue(rootTag.isAncestorOf(childTag));
        assertTrue(rootTag.isAncestorOf(grandChildTag));
        assertTrue(childTag.isAncestorOf(grandChildTag));

        assertFalse(childTag.isAncestorOf(rootTag));
        assertFalse(grandChildTag.isAncestorOf(childTag));
        assertFalse(grandChildTag.isAncestorOf(rootTag));

        // Test self-reference
        assertTrue(rootTag.isAncestorOf(rootTag));
    }

    @Test
    void testIsDescendantOf() {
        assertTrue(childTag.isDescendantOf(rootTag));
        assertTrue(grandChildTag.isDescendantOf(rootTag));
        assertTrue(grandChildTag.isDescendantOf(childTag));

        assertFalse(rootTag.isDescendantOf(childTag));
        assertFalse(childTag.isDescendantOf(grandChildTag));
        assertFalse(rootTag.isDescendantOf(grandChildTag));
    }

    @Test
    void testUpdateSlugAndPath() {
        Tag tag = new Tag();
        tag.setName("Java Programming");
        tag.setParent(rootTag);

        tag.updateSlugAndPath();

        assertEquals("java_programming", tag.getSlug());
        assertEquals("programming.java_programming", tag.getPath());
    }

    @Test
    void testIsAncestorOfWithNullPath() {
        Tag tagWithoutPath = new Tag();
        tagWithoutPath.setName("Test");

        assertFalse(rootTag.isAncestorOf(tagWithoutPath));
        assertFalse(tagWithoutPath.isAncestorOf(rootTag));
    }

    @Test
    void testIsAncestorOfWithNullTag() {
        assertFalse(rootTag.isAncestorOf(null));
    }

    @Test
    void testIsDescendantOfWithNullTag() {
        assertFalse(rootTag.isDescendantOf(null));
    }
}
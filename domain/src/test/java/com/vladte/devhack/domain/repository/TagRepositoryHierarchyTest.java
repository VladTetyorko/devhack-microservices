package com.vladte.devhack.domain.repository;

import com.vladte.devhack.domain.entities.global.Tag;
import com.vladte.devhack.domain.repository.global.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TagRepository hierarchical functionality.
 */
@DataJpaTest
@ActiveProfiles("test")
class TagRepositoryHierarchyTest {

    @Autowired
    private TagRepository tagRepository;

    private Tag rootTag;
    private Tag childTag1;
    private Tag childTag2;
    private Tag grandChildTag;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        tagRepository.deleteAll();

        // Create a test hierarchy:
        // root (programming)
        // ├── child1 (java)
        // │   └── grandchild (spring_boot)
        // └── child2 (python)

        rootTag = new Tag();
        rootTag.setName("Programming");
        rootTag.setSlug("programming");
        rootTag.setPath("programming");
        rootTag = tagRepository.save(rootTag);

        childTag1 = new Tag();
        childTag1.setName("Java");
        childTag1.setSlug("java");
        childTag1.setPath("programming.java");
        childTag1.setParent(rootTag);
        childTag1 = tagRepository.save(childTag1);

        childTag2 = new Tag();
        childTag2.setName("Python");
        childTag2.setSlug("python");
        childTag2.setPath("programming.python");
        childTag2.setParent(rootTag);
        childTag2 = tagRepository.save(childTag2);

        grandChildTag = new Tag();
        grandChildTag.setName("Spring Boot");
        grandChildTag.setSlug("spring_boot");
        grandChildTag.setPath("programming.java.spring_boot");
        grandChildTag.setParent(childTag1);
        grandChildTag = tagRepository.save(grandChildTag);
    }

    @Test
    void testFindBySlug() {
        Optional<Tag> found = tagRepository.findBySlug("java");
        assertTrue(found.isPresent());
        assertEquals("Java", found.get().getName());
    }

    @Test
    void testFindByParentAndSlug() {
        Optional<Tag> found = tagRepository.findByParentAndSlug(rootTag, "java");
        assertTrue(found.isPresent());
        assertEquals("Java", found.get().getName());

        Optional<Tag> notFound = tagRepository.findByParentAndSlug(rootTag, "nonexistent");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindByParentIsNull() {
        List<Tag> rootTags = tagRepository.findByParentIsNull();
        assertEquals(1, rootTags.size());
        assertEquals("Programming", rootTags.get(0).getName());
    }

    @Test
    void testFindByParent() {
        List<Tag> children = tagRepository.findByParent(rootTag);
        assertEquals(2, children.size());

        List<String> childNames = children.stream()
                .map(Tag::getName)
                .sorted()
                .toList();
        assertEquals(List.of("Java", "Python"), childNames);
    }

    @Test
    void testFindDescendants() {
        List<Tag> descendants = tagRepository.findDescendants("programming");
        assertEquals(4, descendants.size()); // root + 2 children + 1 grandchild

        List<Tag> javaDescendants = tagRepository.findDescendants("programming.java");
        assertEquals(2, javaDescendants.size()); // java + spring_boot
    }

    @Test
    void testFindAncestors() {
        List<Tag> ancestors = tagRepository.findAncestors("programming.java.spring_boot");
        assertEquals(3, ancestors.size()); // programming, java, spring_boot

        List<String> ancestorPaths = ancestors.stream()
                .map(Tag::getPath)
                .sorted()
                .toList();
        assertEquals(List.of("programming", "programming.java", "programming.java.spring_boot"), ancestorPaths);
    }

    @Test
    void testFindSubtreeWithDepth() {
        // Find subtree with depth 1 from root (should include root + direct children)
        List<Tag> subtree = tagRepository.findSubtreeWithDepth("programming", 1);
        assertEquals(3, subtree.size()); // programming, java, python

        // Find subtree with depth 0 from root (should include only root)
        List<Tag> rootOnly = tagRepository.findSubtreeWithDepth("programming", 0);
        assertEquals(1, rootOnly.size());
        assertEquals("Programming", rootOnly.get(0).getName());
    }

    @Test
    void testFindParentByPath() {
        Optional<Tag> parent = tagRepository.findParentByPath("programming.java");
        assertTrue(parent.isPresent());
        assertEquals("Programming", parent.get().getName());

        Optional<Tag> grandParent = tagRepository.findParentByPath("programming.java.spring_boot");
        assertTrue(grandParent.isPresent());
        assertEquals("Java", grandParent.get().getName());

        // Root should have no parent
        Optional<Tag> rootParent = tagRepository.findParentByPath("programming");
        assertFalse(rootParent.isPresent());
    }

    @Test
    void testFindByDepth() {
        List<Tag> depthZero = tagRepository.findByDepth(1); // Root level (depth 0 + 1)
        assertEquals(1, depthZero.size());
        assertEquals("Programming", depthZero.get(0).getName());

        List<Tag> depthOne = tagRepository.findByDepth(2); // First level children (depth 1 + 1)
        assertEquals(2, depthOne.size());

        List<Tag> depthTwo = tagRepository.findByDepth(3); // Second level children (depth 2 + 1)
        assertEquals(1, depthTwo.size());
        assertEquals("Spring Boot", depthTwo.get(0).getName());
    }

    @Test
    void testFindSiblings() {
        List<Tag> siblings = tagRepository.findSiblings(rootTag, childTag1.getId());
        assertEquals(1, siblings.size());
        assertEquals("Python", siblings.get(0).getName());

        // Root tag should have no siblings
        List<Tag> rootSiblings = tagRepository.findSiblings(null, rootTag.getId());
        assertEquals(0, rootSiblings.size());
    }

    @Test
    void testExistsByPath() {
        assertTrue(tagRepository.existsByPath("programming"));
        assertTrue(tagRepository.existsByPath("programming.java"));
        assertTrue(tagRepository.existsByPath("programming.java.spring_boot"));
        assertFalse(tagRepository.existsByPath("nonexistent"));
        assertFalse(tagRepository.existsByPath("programming.nonexistent"));
    }

    @Test
    void testUpdateSubtreePaths() {
        // Move java subtree from programming.java to programming.moved_java
        tagRepository.updateSubtreePaths("programming.java", "programming.moved_java");

        // Verify the paths were updated
        Optional<Tag> movedJava = tagRepository.findById(childTag1.getId());
        assertTrue(movedJava.isPresent());
        assertEquals("programming.moved_java", movedJava.get().getPath());

        Optional<Tag> movedSpringBoot = tagRepository.findById(grandChildTag.getId());
        assertTrue(movedSpringBoot.isPresent());
        assertEquals("programming.moved_java.spring_boot", movedSpringBoot.get().getPath());

        // Verify other paths weren't affected
        Optional<Tag> unchangedPython = tagRepository.findById(childTag2.getId());
        assertTrue(unchangedPython.isPresent());
        assertEquals("programming.python", unchangedPython.get().getPath());
    }

    @Test
    void testFindByPathPattern() {
        // Find all tags under programming
        List<Tag> programmingTags = tagRepository.findByPathPattern("programming.*");
        assertEquals(4, programmingTags.size());

        // Find all java-related tags
        List<Tag> javaTags = tagRepository.findByPathPattern("*.java.*");
        assertEquals(2, javaTags.size()); // java and spring_boot

        // Find all second-level tags
        List<Tag> secondLevel = tagRepository.findByPathPattern("*.*{1}");
        assertEquals(2, secondLevel.size()); // java and python
    }

    @Test
    void testHierarchicalConstraints() {
        // Test that we can't create duplicate slugs under same parent
        Tag duplicateChild = new Tag();
        duplicateChild.setName("Java Duplicate");
        duplicateChild.setSlug("java");
        duplicateChild.setPath("programming.java");
        duplicateChild.setParent(rootTag);

        // This should fail due to unique constraint on (parent_id, slug)
        assertThrows(Exception.class, () -> tagRepository.save(duplicateChild));
    }

    @Test
    void testCascadeDelete() {
        // Delete the java tag - should cascade to spring_boot
        tagRepository.delete(childTag1);

        // Verify java and spring_boot are deleted
        assertFalse(tagRepository.findById(childTag1.getId()).isPresent());
        assertFalse(tagRepository.findById(grandChildTag.getId()).isPresent());

        // Verify other tags still exist
        assertTrue(tagRepository.findById(rootTag.getId()).isPresent());
        assertTrue(tagRepository.findById(childTag2.getId()).isPresent());
    }
}
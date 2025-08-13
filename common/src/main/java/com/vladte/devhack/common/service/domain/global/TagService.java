package com.vladte.devhack.common.service.domain.global;

import com.vladte.devhack.common.service.domain.CrudService;
import com.vladte.devhack.entities.global.Tag;
import com.vladte.devhack.entities.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for Tag entity operations with hierarchical support.
 */
public interface TagService extends CrudService<Tag, UUID> {
    /**
     * Find a tag by name.
     *
     * @param name the tag name
     * @return an Optional containing the tag, or empty if not found
     */
    Optional<Tag> findTagByName(String name);

    /**
     * Find a tag by slug.
     *
     * @param slug the tag slug
     * @return an Optional containing the tag, or empty if not found
     */
    Optional<Tag> findTagBySlug(String slug);

    /**
     * Create a new tag with proper path generation.
     *
     * @param name   the tag name
     * @param parent the parent tag (null for root tags)
     * @return the created tag
     */
    Tag createTag(String name, Tag parent);

    /**
     * Move a tag to a new parent, updating all subtree paths.
     *
     * @param tag       the tag to move
     * @param newParent the new parent (null to make it a root tag)
     * @return the updated tag
     */
    Tag moveTag(Tag tag, Tag newParent);

    /**
     * Delete a tag with cascade or orphan handling policy.
     *
     * @param tag           the tag to delete
     * @param cascadeDelete if true, delete all children; if false, move children to root
     * @return true if deletion was successful
     */
    boolean deleteTag(Tag tag, boolean cascadeDelete);

    /**
     * Find all root tags (tags with no parent).
     *
     * @return list of root tags
     */
    List<Tag> findRootTags();

    /**
     * Find all direct children of a tag.
     *
     * @param parent the parent tag
     * @return list of direct children
     */
    List<Tag> findChildren(Tag parent);

    /**
     * Find all descendants of a tag (entire subtree).
     *
     * @param parent the parent tag
     * @return list of all descendants
     */
    List<Tag> findDescendants(Tag parent);

    /**
     * Find all ancestors of a tag.
     *
     * @param tag the tag
     * @return list of ancestors from root to immediate parent
     */
    List<Tag> findAncestors(Tag tag);

    /**
     * Find subtree limited by depth.
     *
     * @param parent the parent tag
     * @param depth  maximum depth to include
     * @return list of tags in subtree within depth limit
     */
    List<Tag> findSubtree(Tag parent, int depth);

    /**
     * Find siblings of a tag (tags with same parent).
     *
     * @param tag the tag
     * @return list of sibling tags
     */
    List<Tag> findSiblings(Tag tag);

    /**
     * Find tags at a specific depth level.
     *
     * @param depth the depth level (0 for root)
     * @return list of tags at the specified depth
     */
    List<Tag> findTagsByDepth(int depth);

    /**
     * Validate that moving a tag won't create a cycle.
     *
     * @param tag       the tag to move
     * @param newParent the proposed new parent
     * @return true if the move is valid (no cycle)
     */
    boolean validateMove(Tag tag, Tag newParent);

    /**
     * Calculate progress for a tag based on the user's answered questions.
     *
     * @param tag  the tag to calculate progress for
     * @param user the user to calculate progress for
     * @return the tag with updated progress information
     */
    Tag calculateProgress(Tag tag, User user);

    /**
     * Calculate progress for all tags based on the user's answered questions.
     *
     * @param tags the list of tags to calculate progress for
     * @param user the user to calculate progress for
     * @return the list of tags with updated progress information
     */
    List<Tag> calculateProgressForAll(List<Tag> tags, User user);

    /**
     * Count tags used by a specific user in their questions.
     *
     * @param user the user
     * @return the count of tags used by the user
     */
    int countTagsByUser(User user);

    /**
     * Count all tags in the system.
     *
     * @return the total count of tags
     */
    int countAllTags();
}

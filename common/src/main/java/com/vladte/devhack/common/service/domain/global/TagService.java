package com.vladte.devhack.common.service.domain.global;

import com.vladte.devhack.common.service.domain.CrudService;
import com.vladte.devhack.entities.global.Tag;
import com.vladte.devhack.entities.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for Tag entity operations.
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

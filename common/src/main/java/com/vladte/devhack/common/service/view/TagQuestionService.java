package com.vladte.devhack.common.service.view;

import com.vladte.devhack.domain.entities.global.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;

/**
 * Service interface for handling operations related to questions filtered by tags.
 * This interface follows the Single Responsibility Principle by focusing only on tag-related question operations.
 */
public interface TagQuestionService {

    /**
     * Prepare the model for displaying questions filtered by a tag.
     *
     * @param tagSlug the slug of the tag to filter by
     * @param model   the model to add attributes to
     * @return the tag that was found, or null if not found
     * @deprecated Use {@link #prepareQuestionsByTagModel(String, Pageable, Model)} instead
     */
    @Deprecated
    Tag prepareQuestionsByTagModel(String tagSlug, Model model);

    /**
     * Prepare the model for displaying questions filtered by a tag with pagination.
     *
     * @param tagSlug  the slug of the tag to filter by
     * @param pageable the pagination information
     * @param model    the model to add attributes to
     * @return the tag that was found, or null if not found
     */
    Tag prepareQuestionsByTagModel(String tagSlug, Pageable pageable, Model model);

    /**
     * Set the page title for the questions filtered by tag page.
     *
     * @param model   the model to add the title to
     * @param tagName the name of the tag
     */
    void setQuestionsByTagPageTitle(Model model, String tagName);

    /**
     * Get a tag by slug, throwing an exception if not found.
     *
     * @param tagSlug      the slug of the tag to find
     * @param errorMessage the error message to use if the tag is not found
     * @return the tag that was found
     * @throws RuntimeException if the tag is not found
     */
    Tag getTagBySlugOrThrow(String tagSlug, String errorMessage);
}

package com.vladte.devhack.common.service.domain;

import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for InterviewQuestion entity operations.
 */
public interface InterviewQuestionService extends BaseService<InterviewQuestion, UUID> {

    /**
     * Find questions by tag.
     *
     * @param tag the tag
     * @return a list of questions with the specified tag
     */
    List<InterviewQuestion> findQuestionsByTag(Tag tag);

    /**
     * Find questions by tag with pagination.
     *
     * @param tag      the tag
     * @param pageable pagination information
     * @return a page of questions with the specified tag
     */
    Page<InterviewQuestion> findQuestionsByTag(Tag tag, Pageable pageable);

    /**
     * Search questions with filtering and pagination.
     *
     * @param query      the search query
     * @param difficulty the difficulty level
     * @param tagId      the tag ID
     * @param pageable   pagination information
     * @return a page of questions matching the search criteria
     */
    Page<InterviewQuestion> searchQuestions(String query, String difficulty, UUID tagId, Pageable pageable);
}

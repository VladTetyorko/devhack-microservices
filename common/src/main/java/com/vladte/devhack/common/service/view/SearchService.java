package com.vladte.devhack.common.service.view;

import com.vladte.devhack.entities.global.InterviewQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for search operations.
 * This interface follows the Single Responsibility Principle by focusing only on search-related operations.
 */
public interface SearchService {

    /**
     * Search for questions with filtering and pagination.
     *
     * @param query      the search query
     * @param difficulty the difficulty level
     * @param tagId      the tag ID
     * @param pageable   pagination information
     * @return a page of questions matching the search criteria
     */
    Page<InterviewQuestion> searchQuestions(String query, String difficulty, UUID tagId, Pageable pageable);

    /**
     * Build a page title based on search parameters.
     *
     * @param query      the search query
     * @param difficulty the difficulty level
     * @param tagId      the tag ID
     * @return a page title string
     */
    String buildSearchPageTitle(String query, String difficulty, UUID tagId);
}
package com.vladte.devhack.common.service.view;

import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;

import java.util.UUID;

/**
 * Service interface for preparing the search view.
 * This interface follows the Single Responsibility Principle by focusing only on search view preparation.
 */
public interface SearchViewService {

    /**
     * Prepare the model for the search results view.
     *
     * @param query      the search query
     * @param difficulty the difficulty to filter by
     * @param tagId      the tag ID to filter by
     * @param page       the page number
     * @param size       the page size
     * @param model      the model to add attributes to
     * @return the pageable object created for the search
     */
    Pageable prepareSearchResultsModel(String query, String difficulty, UUID tagId, int page, int size, Model model);

    /**
     * Set the page title for the search results page.
     *
     * @param model      the model to add the title to
     * @param query      the search query
     * @param difficulty the difficulty to filter by
     * @param tagId      the tag ID to filter by
     */
    void setSearchResultsPageTitle(Model model, String query, String difficulty, UUID tagId);
}

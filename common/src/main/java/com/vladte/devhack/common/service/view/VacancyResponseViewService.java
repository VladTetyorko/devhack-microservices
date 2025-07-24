package com.vladte.devhack.common.service.view;

import com.vladte.devhack.common.model.dto.VacancyResponseDTO;
import com.vladte.devhack.entities.user.User;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;

import java.util.UUID;

/**
 * Service interface for preparing the vacancy response view.
 * This interface follows the Single Responsibility Principle by focusing only on view preparation.
 */
public interface VacancyResponseViewService {

    /**
     * Prepare the model for the list view of vacancy responses for the current user.
     *
     * @param page  the page number
     * @param size  the page size
     * @param model the model to add attributes to
     * @return the page of vacancy response DTOs
     */
    Page<VacancyResponseDTO> prepareCurrentUserVacancyResponsesModel(int page, int size, Model model);

    /**
     * Set the page title for the current user's vacancy responses page.
     *
     * @param model the model to add the title to
     */
    void setCurrentUserVacancyResponsesPageTitle(Model model);

    /**
     * Prepare the model for the search results view.
     *
     * @param query the search query
     * @param stage the interview stage to filter by
     * @param page  the page number
     * @param size  the page size
     * @param model the model to add attributes to
     */
    void prepareSearchResultsModel(String query, String stage, int page, int size, Model model);

    /**
     * Set the page title for the search results page.
     *
     * @param model the model to add the title to
     */
    void setSearchResultsPageTitle(Model model);

    /**
     * Prepare the model for the user-specific vacancy responses view.
     *
     * @param userId the ID of the user to find vacancy responses for
     * @param page   the page number
     * @param size   the page size
     * @param model  the model to add attributes to
     * @return the user whose vacancy responses are being displayed
     */
    User prepareUserVacancyResponsesModel(UUID userId, int page, int size, Model model);

    /**
     * Set the page title for the user-specific vacancy responses page.
     *
     * @param model the model to add the title to
     * @param user  the user whose vacancy responses are being displayed
     */
    void setUserVacancyResponsesPageTitle(Model model, User user);
}

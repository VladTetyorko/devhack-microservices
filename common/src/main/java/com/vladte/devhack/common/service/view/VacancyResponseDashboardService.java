package com.vladte.devhack.common.service.view;

import com.vladte.devhack.common.dto.VacancyResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;

/**
 * Service interface for preparing the vacancy response dashboard view.
 * This interface follows the Single Responsibility Principle by focusing only on dashboard view preparation.
 */
public interface VacancyResponseDashboardService {

    /**
     * Prepare the model for the dashboard view with pagination for the vacancy responses.
     *
     * @param page  the page number
     * @param size  the page size
     * @param model the model to add attributes to
     * @return the page of vacancy response DTOs
     */
    Page<VacancyResponseDTO> prepareDashboardModel(int page, int size, Model model);

    /**
     * Set the page title for the dashboard page.
     *
     * @param model the model to add the title to
     */
    void setDashboardPageTitle(Model model);
}

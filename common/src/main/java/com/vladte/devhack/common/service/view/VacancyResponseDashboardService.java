package com.vladte.devhack.common.service.view;

import com.vladte.devhack.entities.user.User;
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
     */
    void prepareDashboardModel(int page, int size, Model model);

    /**
     * Set the page title for the dashboard page.
     *
     * @param model the model to add the title to
     */
    void setDashboardPageTitle(Model model);

    /**
     * Prepare the model for the Jira board view with vacancy responses grouped by interview stage.
     *
     * @param model the model to add attributes to
     */
    void prepareBoardModel(Model model, User currentUser, Integer stageIndex);

    /**
     * Prepare the model for the Jira board view with vacancy responses grouped by interview stage category.
     *
     * @param model        the model to add attributes to
     * @param currentUser  the current user
     * @param categoryCode the code of the interview stage category to display (optional)
     */
    void prepareBoardModelByCategory(Model model, User currentUser, String categoryCode);
}

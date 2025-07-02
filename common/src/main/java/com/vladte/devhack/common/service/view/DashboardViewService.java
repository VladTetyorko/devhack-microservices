package com.vladte.devhack.common.service.view;

import org.springframework.ui.Model;

/**
 * Service interface for preparing the dashboard view.
 * This interface follows the Single Responsibility Principle by focusing only on dashboard view preparation.
 */
public interface DashboardViewService {

    /**
     * Prepare the model for the dashboard view.
     * This method adds all necessary attributes to the model for rendering the dashboard.
     *
     * @param model the model to add attributes to
     */
    void prepareDashboardModel(Model model);

    /**
     * Set the page title for the dashboard.
     *
     * @param model the model to add the title to
     */
    void setDashboardPageTitle(Model model);
}
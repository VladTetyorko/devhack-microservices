package com.vladte.devhack.common.service.view;

import org.springframework.ui.Model;

/**
 * Base service interface for view-related operations.
 * This interface follows the Single Responsibility Principle by focusing only on view-related operations.
 */
public interface BaseViewService {

    /**
     * Set the page title in the model.
     *
     * @param model the model to add the title to
     * @param title the page title
     */
    void setPageTitle(Model model, String title);

    /**
     * Get the model attribute name for the entity list.
     *
     * @param entityName the name of the entity
     * @return the model attribute name for the entity list
     */
    String getModelAttributeName(String entityName);

    /**
     * Get the model attribute name for the entity or entity list.
     *
     * @param entityName the name of the entity
     * @param plural     whether the name should be plural
     * @return the model attribute name
     */
    String getModelAttributeName(String entityName, boolean plural);
}
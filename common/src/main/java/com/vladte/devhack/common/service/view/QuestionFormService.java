package com.vladte.devhack.common.service.view;

import org.springframework.ui.Model;

/**
 * Service interface for handling question form operations.
 * This interface follows the Single Responsibility Principle by focusing only on form-related operations.
 */
public interface QuestionFormService {

    /**
     * Prepare the model for generating questions using AI.
     *
     * @param model the model to add attributes to
     */
    void prepareGenerateQuestionsForm(Model model);

    /**
     * Prepare the model for auto-generating easy questions using AI.
     *
     * @param model the model to add attributes to
     */
    void prepareAutoGenerateQuestionsForm(Model model);

    /**
     * Set the page title for the auto-generate questions form.
     *
     * @param model the model to add the title to
     */
    void setAutoGenerateQuestionsPageTitle(Model model);
}

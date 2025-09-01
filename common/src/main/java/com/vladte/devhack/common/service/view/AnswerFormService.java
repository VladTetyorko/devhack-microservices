package com.vladte.devhack.common.service.view;

import com.vladte.devhack.domain.model.dto.personalized.AnswerDTO;
import org.springframework.ui.Model;

import java.util.UUID;

/**
 * Service interface for handling answer form operations.
 * This interface follows the Single Responsibility Principle by focusing only on form-related operations.
 */
public interface AnswerFormService {

    /**
     * Prepare the model for creating a new answer.
     *
     * @param questionId the ID of the question being answered (optional)
     * @param model      the model to add attributes to
     */
    void prepareNewAnswerForm(UUID questionId, Model model);

    /**
     * Prepare the model for editing an existing answer.
     *
     * @param id    the ID of the answer to edit
     * @param model the model to add attributes to
     * @return the answer DTO being edited, or null if not found
     */
    AnswerDTO prepareEditAnswerForm(UUID id, Model model);

    /**
     * Save an answer from form submission.
     *
     * @param answerDTO  the answer data from the form
     * @param userId     the ID of the user who created the answer
     * @param questionId the ID of the question being answered
     * @return the saved answer DTO
     */
    AnswerDTO saveAnswer(AnswerDTO answerDTO, UUID userId, UUID questionId);

    /**
     * Delete an answer by ID.
     *
     * @param id the ID of the answer to delete
     */
    void deleteAnswer(UUID id);

    /**
     * Asynchronously check an answer using AI and update its score and feedback.
     * This method only sends the request and doesn't wait for the response.
     *
     * @param id the ID of the answer to check
     */
    void checkAnswerWithAiAsync(UUID id);

    /**
     * Set the page title for the new answer form.
     *
     * @param model the model to add the title to
     */
    void setNewAnswerPageTitle(Model model);

    /**
     * Set the page title for the edit answer form.
     *
     * @param model the model to add the title to
     */
    void setEditAnswerPageTitle(Model model);
}

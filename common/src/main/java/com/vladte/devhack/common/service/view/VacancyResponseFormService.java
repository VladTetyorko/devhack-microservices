package com.vladte.devhack.common.service.view;

import com.vladte.devhack.domain.model.dto.personalized.VacancyResponseDTO;
import org.springframework.ui.Model;

import java.util.UUID;

/**
 * Service interface for handling vacancy response form operations.
 * This interface follows the Single Responsibility Principle by focusing only on form-related operations.
 */
public interface VacancyResponseFormService {

    /**
     * Prepare the model for creating a new vacancy response.
     *
     * @param model the model to add attributes to
     */
    void prepareNewVacancyResponseForm(Model model);

    /**
     * Prepare the model for editing an existing vacancy response.
     *
     * @param id    the ID of the vacancy response to edit
     * @param model the model to add attributes to
     * @return the vacancy response DTO being edited, or null if not found
     */
    VacancyResponseDTO prepareEditVacancyResponseForm(UUID id, Model model);

    /**
     * Save a vacancy response from form submission.
     *
     * @param vacancyResponseDTO the vacancy response data from the form
     * @param userId             the ID of the user to associate with the vacancy response
     * @return the saved vacancy response DTO
     */
    VacancyResponseDTO saveVacancyResponse(VacancyResponseDTO vacancyResponseDTO, UUID userId);

    VacancyResponseDTO saveVacancyResponse(VacancyResponseDTO vacancyResponseDTO, UUID userId, UUID interviewStageId);

    /**
     * Delete a vacancy response by ID.
     *
     * @param id the ID of the vacancy response to delete
     */
    void deleteVacancyResponse(UUID id);

    /**
     * Set the page title for the new vacancy response form.
     *
     * @param model the model to add the title to
     */
    void setNewVacancyResponsePageTitle(Model model);

    /**
     * Set the page title for the edit vacancy response form.
     *
     * @param model the model to add the title to
     */
    void setEditVacancyResponsePageTitle(Model model);
}

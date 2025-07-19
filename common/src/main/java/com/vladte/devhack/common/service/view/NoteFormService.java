package com.vladte.devhack.common.service.view;

import com.vladte.devhack.common.model.dto.NoteDTO;
import org.springframework.ui.Model;

import java.util.UUID;

/**
 * Service interface for handling note form operations.
 * This interface follows the Single Responsibility Principle by focusing only on form-related operations.
 */
public interface NoteFormService {

    /**
     * Prepare the model for creating a new note.
     *
     * @param questionId the ID of the question being linked to the note (optional)
     * @param model      the model to add attributes to
     */
    void prepareNewNoteForm(UUID questionId, Model model);

    /**
     * Prepare the model for editing an existing note.
     *
     * @param id    the ID of the note to edit
     * @param model the model to add attributes to
     * @return the note DTO being edited, or null if not found
     */
    NoteDTO prepareEditNoteForm(UUID id, Model model);

    /**
     * Save a note from form submission.
     *
     * @param noteDTO    the note data from the form
     * @param userId     the ID of the user who created the note
     * @param questionId the ID of the question linked to the note
     * @return the saved note DTO
     */
    NoteDTO saveNote(NoteDTO noteDTO, UUID userId, UUID questionId);

    /**
     * Delete a note by ID.
     *
     * @param id the ID of the note to delete
     */
    void deleteNote(UUID id);

    /**
     * Set the page title for the new note form.
     *
     * @param model the model to add the title to
     */
    void setNewNotePageTitle(Model model);

    /**
     * Set the page title for the edit note form.
     *
     * @param model the model to add the title to
     */
    void setEditNotePageTitle(Model model);
}
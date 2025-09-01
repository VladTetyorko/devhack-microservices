package com.vladte.devhack.domain.service.personalized;

import com.vladte.devhack.domain.entities.global.InterviewQuestion;
import com.vladte.devhack.domain.entities.personalized.Note;
import com.vladte.devhack.domain.entities.user.User;
import com.vladte.devhack.domain.service.CrudService;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Note entity operations.
 */
public interface NoteService extends CrudService<Note, UUID> {
    /**
     * Find notes by user.
     *
     * @param user the user
     * @return a list of notes by the user
     */
    List<Note> findNotesByUser(User user);

    /**
     * Find notes by linked question.
     *
     * @param question the question
     * @return a list of notes linked to the question
     */
    List<Note> findNotesByLinkedQuestion(InterviewQuestion question);

    /**
     * Find notes by user and linked question.
     *
     * @param user     the user
     * @param question the question
     * @return a list of notes by the user and linked to the question
     */
    List<Note> findNotesByUserAndLinkedQuestion(User user, InterviewQuestion question);
}

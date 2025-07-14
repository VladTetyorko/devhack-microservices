package com.vladte.devhack.common.service.domain.impl;

import com.vladte.devhack.common.repository.NoteRepository;
import com.vladte.devhack.common.service.domain.NoteService;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.Note;
import com.vladte.devhack.entities.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the NoteService interface.
 */
@Service
public class NoteServiceImpl extends UserOwnedServiceImpl<Note, UUID, NoteRepository> implements NoteService {

    /**
     * Constructor with repository injection.
     *
     * @param repository the note repository
     */

    public NoteServiceImpl(NoteRepository repository) {
        super(repository);
    }

    @Override
    public List<Note> findNotesByUser(User user) {
        return repository.findByUser(user);
    }

    @Override
    public List<Note> findNotesByLinkedQuestion(InterviewQuestion question) {
        return repository.findByQuestion(question);
    }

    @Override
    public List<Note> findNotesByUserAndLinkedQuestion(User user, InterviewQuestion question) {
        return repository.findByUserAndQuestion(user, question);
    }

    @Override
    protected User getEntityUser(Note entity) {
        return entity.getUser();
    }
}

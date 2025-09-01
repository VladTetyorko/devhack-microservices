package com.vladte.devhack.domain.service.personalized.impl;

import com.vladte.devhack.domain.entities.global.InterviewQuestion;
import com.vladte.devhack.domain.entities.personalized.Note;
import com.vladte.devhack.domain.entities.user.User;
import com.vladte.devhack.domain.repository.personalized.NoteRepository;
import com.vladte.devhack.domain.service.audit.AuditService;
import com.vladte.devhack.domain.service.personalized.NoteService;
import com.vladte.devhack.domain.service.personalized.PersonalizedService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the NoteService interface.
 */
@Service
public class NoteServiceImpl extends PersonalizedService<Note, UUID, NoteRepository> implements NoteService {

    /**
     * Constructor with repository injection.
     *
     * @param repository the note repository
     */

    public NoteServiceImpl(NoteRepository repository, AuditService auditService) {
        super(repository, auditService);
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

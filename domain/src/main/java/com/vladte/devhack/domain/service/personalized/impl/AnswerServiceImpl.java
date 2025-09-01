package com.vladte.devhack.domain.service.personalized.impl;

import com.vladte.devhack.domain.entities.global.InterviewQuestion;
import com.vladte.devhack.domain.entities.personalized.Answer;
import com.vladte.devhack.domain.entities.user.User;
import com.vladte.devhack.domain.repository.personalized.AnswerRepository;
import com.vladte.devhack.domain.service.audit.AuditService;
import com.vladte.devhack.domain.service.personalized.AnswerService;
import com.vladte.devhack.domain.service.personalized.PersonalizedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the AnswerService interface.
 */
@Service
public class AnswerServiceImpl extends PersonalizedService<Answer, UUID, AnswerRepository> implements AnswerService {

    private static final Logger log = LoggerFactory.getLogger(AnswerServiceImpl.class);

    /**
     * Constructor with repository and service injection.
     *
     * @param repository the answer repository
     */

    public AnswerServiceImpl(
            AnswerRepository repository,
            AuditService auditService) {
        super(repository, auditService);
    }


    @Override
    public List<Answer> findAnswersByUser(User user) {
        log.debug("Finding answers for user ID: {}", user.getId());
        List<Answer> answers = repository.findByUser(user);
        log.debug("Found {} answers for user ID: {}", answers.size(), user.getId());
        return answers;
    }

    @Override
    public Page<Answer> findAnswersByUser(User user, Pageable pageable) {
        log.debug("Finding answers for user ID: {} with pagination", user.getId());
        Page<Answer> answerPage = repository.findByUser(user, pageable);
        log.debug("Found {} answers for user ID: {} (page {} of {})",
                answerPage.getNumberOfElements(), user.getId(),
                pageable.getPageNumber(), answerPage.getTotalPages());
        return answerPage;
    }

    @Override
    public List<Answer> findAnswersByQuestion(InterviewQuestion question) {
        log.debug("Finding answers for question ID: {}", question.getId());
        List<Answer> answers = repository.findByQuestion(question);
        log.debug("Found {} answers for question ID: {}", answers.size(), question.getId());
        return answers;
    }

    @Override
    public Page<Answer> findAnswersByQuestion(InterviewQuestion question, Pageable pageable) {
        log.debug("Finding answers for question ID: {} with pagination", question.getId());
        Page<Answer> answerPage = repository.findByQuestion(question, pageable);
        log.debug("Found {} answers for question ID: {} (page {} of {})",
                answerPage.getNumberOfElements(), question.getId(),
                pageable.getPageNumber(), answerPage.getTotalPages());
        return answerPage;
    }

    @Override
    public List<Answer> findAnswersByUserAndQuestion(User user, InterviewQuestion question) {
        log.debug("Finding answers for user ID: {} and question ID: {}", user.getId(), question.getId());
        List<Answer> answers = repository.findByUserAndQuestion(user, question);
        log.debug("Found {} answers for user ID: {} and question ID: {}",
                answers.size(), user.getId(), question.getId());
        return answers;
    }

    @Override
    protected User getEntityUser(Answer entity) {
        return entity.getUser();
    }
}

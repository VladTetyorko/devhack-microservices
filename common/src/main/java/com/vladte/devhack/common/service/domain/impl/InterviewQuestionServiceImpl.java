package com.vladte.devhack.common.service.domain.impl;

import com.vladte.devhack.common.repository.InterviewQuestionRepository;
import com.vladte.devhack.common.service.domain.InterviewQuestionService;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.Tag;
import com.vladte.devhack.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the InterviewQuestionService interface.
 */
@Service
public class InterviewQuestionServiceImpl extends BaseServiceImpl<InterviewQuestion, UUID, InterviewQuestionRepository> implements InterviewQuestionService {

    /**
     * Constructor with repository injection.
     *
     * @param repository the interview question repository
     */
    @Autowired
    public InterviewQuestionServiceImpl(InterviewQuestionRepository repository) {
        super(repository);
    }

    @Override
    public List<InterviewQuestion> findQuestionsByTag(Tag tag) {
        return repository.findByTagsContaining(tag);
    }

    @Override
    public Page<InterviewQuestion> findQuestionsByTag(Tag tag, Pageable pageable) {
        return repository.findByTagsContaining(tag, pageable);
    }

    @Override
    public Page<InterviewQuestion> searchQuestions(String query, String difficulty, UUID tagId, Pageable pageable) {
        return repository.searchQuestions(query, difficulty, tagId, pageable);
    }

    @Override
    public int countQuestionsByUser(User user) {
        if (user == null) {
            return 0;
        }
        return (int) findAll().stream()
                .filter(question -> question.getUser() != null && question.getUser().getId().equals(user.getId()))
                .count();
    }

    @Override
    public int countAllQuestions() {
        return findAll().size();
    }

    @Override
    public int findAnsweredQuestionsByUser(User user) {
        if (user == null) {
            return 0;
        }
        return repository.countAnsweredQuestionsByUserId(user.getId());
    }


}

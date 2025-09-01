package com.vladte.devhack.domain.service.personalized;

import com.vladte.devhack.domain.entities.global.InterviewQuestion;
import com.vladte.devhack.domain.entities.personalized.Answer;
import com.vladte.devhack.domain.entities.user.User;
import com.vladte.devhack.domain.service.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Answer entity operations.
 */
public interface AnswerService extends CrudService<Answer, UUID> {
    /**
     * Find answers by user.
     *
     * @param user the user
     * @return a list of answers by the user
     */
    List<Answer> findAnswersByUser(User user);

    /**
     * Find answers by user with pagination.
     *
     * @param user     the user
     * @param pageable the pagination information
     * @return a page of answers by the user
     */
    Page<Answer> findAnswersByUser(User user, Pageable pageable);

    /**
     * Find answers by question.
     *
     * @param question the question
     * @return a list of answers for the question
     * @deprecated Use {@link #findAnswersByQuestion(InterviewQuestion, Pageable)} instead
     */
    @Deprecated
    List<Answer> findAnswersByQuestion(InterviewQuestion question);

    /**
     * Find answers by question with pagination.
     *
     * @param question the question
     * @param pageable the pagination information
     * @return a page of answers for the question
     */
    Page<Answer> findAnswersByQuestion(InterviewQuestion question, Pageable pageable);

    /**
     * Find answers by user and question.
     *
     * @param user     the user
     * @param question the question
     * @return a list of answers by the user for the question
     */
    List<Answer> findAnswersByUserAndQuestion(User user, InterviewQuestion question);


}

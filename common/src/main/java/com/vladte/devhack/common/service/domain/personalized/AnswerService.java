package com.vladte.devhack.common.service.domain.personalized;

import com.vladte.devhack.common.service.domain.CrudService;
import com.vladte.devhack.entities.Answer;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

    /**
     * Check an answer using AI and update its score.
     *
     * @param answerId the ID of the answer to check
     * @return the updated answer with the AI score
     */
    Answer checkAnswerWithAi(UUID answerId);

    /**
     * Check an answer using AI and update its score asynchronously.
     *
     * @param answerId the ID of the answer to check
     * @return a CompletableFuture containing the updated answer with the AI score
     */
    @Async
    CompletableFuture<Answer> checkAnswerWithAiAsync(UUID answerId);
}

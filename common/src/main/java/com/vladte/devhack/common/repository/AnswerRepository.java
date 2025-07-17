package com.vladte.devhack.common.repository;

import com.vladte.devhack.entities.Answer;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, UUID> {
    // Custom query methods
    List<Answer> findByUser(User user);

    List<Answer> findByQuestion(InterviewQuestion question);

    List<Answer> findByUserAndQuestion(User user, InterviewQuestion question);

    // Paginated versions of the query methods
    Page<Answer> findByUser(User user, Pageable pageable);

    Page<Answer> findByQuestion(InterviewQuestion question, Pageable pageable);

    Page<Answer> findByUserAndQuestion(User user, InterviewQuestion question, Pageable pageable);

    // Search method with pagination
    @Query("SELECT a FROM Answer a WHERE " +
            "(:query IS NULL OR LOWER(a.text) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:userId IS NULL OR a.user.id = :userId) AND " +
            "(:questionId IS NULL OR a.question.id = :questionId)")
    Page<Answer> searchAnswers(
            @Param("query") String query,
            @Param("userId") UUID userId,
            @Param("questionId") UUID questionId,
            Pageable pageable);

    @Query("SELECT a FROM Answer a JOIN FETCH a.question WHERE a.id = :id")
    Optional<Answer> findByIdWithQuestion(@Param("id") UUID id);
}

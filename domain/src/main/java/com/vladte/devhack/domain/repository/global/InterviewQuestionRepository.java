package com.vladte.devhack.domain.repository.global;

import com.vladte.devhack.domain.entities.global.InterviewQuestion;
import com.vladte.devhack.domain.entities.global.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, UUID>, JpaSpecificationExecutor<InterviewQuestion> {
    // Custom query methods for filtering
    List<InterviewQuestion> findByDifficulty(String difficulty);

    List<InterviewQuestion> findByTagsContaining(Tag tag);

    List<InterviewQuestion> findByDifficultyAndTagsContaining(String difficulty, Tag tag);

    // Paginated versions of the query methods
    Page<InterviewQuestion> findByDifficulty(String difficulty, Pageable pageable);

    Page<InterviewQuestion> findByTagsContaining(Tag tag, Pageable pageable);

    Page<InterviewQuestion> findByDifficultyAndTagsContaining(String difficulty, Tag tag, Pageable pageable);

    @Query(value = "SELECT COUNT(*) " +
            "FROM interview_questions iq " +
            "WHERE EXISTS ( " +
            "    SELECT 1 " +
            "    FROM answers a " +
            "    INNER JOIN users u ON a.user_id = u.id " +
            "    WHERE u.id = :userId AND a.question_id = iq.id " +
            ")", nativeQuery = true)
    int countQuestionsWithAnswerByUserId(@Param("userId") UUID userId);

    int countInterviewQuestionsByUserId(UUID userId);
}

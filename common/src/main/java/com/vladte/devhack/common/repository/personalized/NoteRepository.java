package com.vladte.devhack.common.repository.personalized;

import com.vladte.devhack.entities.global.InterviewQuestion;
import com.vladte.devhack.entities.personalized.Note;
import com.vladte.devhack.entities.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    // Custom query methods
    List<Note> findByUser(User user);

    List<Note> findByQuestion(InterviewQuestion question);

    List<Note> findByUserAndQuestion(User user, InterviewQuestion question);

    // Paginated versions of the query methods
    Page<Note> findByUser(User user, Pageable pageable);

    Page<Note> findByQuestion(InterviewQuestion question, Pageable pageable);

    Page<Note> findByUserAndQuestion(User user, InterviewQuestion question, Pageable pageable);

    // Search method with pagination
    @Query("SELECT n FROM Note n WHERE " +
            "(:query IS NULL OR LOWER(n.noteText) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:userId IS NULL OR n.user.id = :userId) AND " +
            "(:questionId IS NULL OR n.question.id = :questionId)")
    Page<Note> searchNotes(
            @Param("query") String query,
            @Param("userId") UUID userId,
            @Param("questionId") UUID questionId,
            Pageable pageable);
}

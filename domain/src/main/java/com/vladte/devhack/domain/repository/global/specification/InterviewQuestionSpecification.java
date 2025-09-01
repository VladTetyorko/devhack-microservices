package com.vladte.devhack.domain.repository.global.specification;

import com.vladte.devhack.domain.entities.global.InterviewQuestion;
import com.vladte.devhack.domain.entities.global.Tag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Specification class for InterviewQuestion entities.
 * This class provides static methods to create specifications for filtering InterviewQuestion entities.
 * Replaces the custom JPQL query in InterviewQuestionRepository.
 */
public class InterviewQuestionSpecification {

    /**
     * Create a specification to filter InterviewQuestion entities by question text.
     * Performs case-insensitive search in the questionText field.
     *
     * @param query the search query for question text
     * @return a specification for filtering by question text
     */
    public static Specification<InterviewQuestion> byQuestionTextContainingIgnoreCase(String query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (query == null || query.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("questionText")),
                    "%" + query.toLowerCase() + "%"
            );
        };
    }

    /**
     * Create a specification to filter InterviewQuestion entities by difficulty.
     *
     * @param difficulty the difficulty level to filter by
     * @return a specification for filtering by difficulty
     */
    public static Specification<InterviewQuestion> byDifficulty(String difficulty) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (difficulty == null || difficulty.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("difficulty"), difficulty);
        };
    }

    /**
     * Create a specification to filter InterviewQuestion entities by tag ID.
     * Uses EXISTS clause to check if the question has a tag with the specified ID.
     *
     * @param tagId the tag ID to filter by
     * @return a specification for filtering by tag ID
     */
    public static Specification<InterviewQuestion> byTagId(UUID tagId) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (tagId == null) {
                return criteriaBuilder.conjunction();
            }

            Join<InterviewQuestion, Tag> tagJoin = root.join("tags", JoinType.INNER);
            return criteriaBuilder.equal(tagJoin.get("id"), tagId);
        };
    }

    /**
     * Create a specification to search InterviewQuestion entities by query, difficulty, and tag ID.
     * This replaces the custom JPQL query in the repository.
     * Combines all three filters with AND logic.
     *
     * @param query      the search query for question text
     * @param difficulty the difficulty level to filter by
     * @param tagId      the tag ID to filter by
     * @return a specification for searching by all parameters
     */
    public static Specification<InterviewQuestion> searchQuestions(String query, String difficulty, UUID tagId) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Predicate queryPredicate = byQuestionTextContainingIgnoreCase(query)
                    .toPredicate(root, criteriaQuery, criteriaBuilder);
            Predicate difficultyPredicate = byDifficulty(difficulty)
                    .toPredicate(root, criteriaQuery, criteriaBuilder);
            Predicate tagPredicate = byTagId(tagId)
                    .toPredicate(root, criteriaQuery, criteriaBuilder);

            return criteriaBuilder.and(queryPredicate, difficultyPredicate, tagPredicate);
        };
    }
}
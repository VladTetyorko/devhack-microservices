package com.vladte.devhack.common.repository.specification;

import com.vladte.devhack.entities.InterviewStage;
import com.vladte.devhack.entities.User;
import com.vladte.devhack.entities.Vacancy;
import com.vladte.devhack.entities.VacancyResponse;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Specification class for VacancyResponse entities.
 * This class provides static methods to create specifications for filtering VacancyResponse entities.
 */
public class VacancyResponseSpecification {

    /**
     * Create a specification to filter VacancyResponse entities by user.
     *
     * @param user the user to filter by
     * @return a specification for filtering by user
     */
    public static Specification<VacancyResponse> byUser(User user) {
        return (root, query, criteriaBuilder) -> {
            if (user == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("user"), user);
        };
    }

    /**
     * Create a specification to filter VacancyResponse entities by user ID.
     *
     * @param userId the user ID to filter by
     * @return a specification for filtering by user ID
     */
    public static Specification<VacancyResponse> byUserId(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("user").get("id"), userId);
        };
    }

    /**
     * Create a specification to filter VacancyResponse entities by company name.
     *
     * @param companyName the company name to filter by
     * @return a specification for filtering by company name
     */
    public static Specification<VacancyResponse> byCompanyNameContainingIgnoreCase(String companyName) {
        return (root, query, criteriaBuilder) -> {
            if (companyName == null || companyName.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("companyName")),
                    "%" + companyName.toLowerCase() + "%"
            );
        };
    }

    /**
     * Create a specification to filter VacancyResponse entities by position.
     *
     * @param position the position to filter by
     * @return a specification for filtering by position
     */
    public static Specification<VacancyResponse> byPositionContainingIgnoreCase(String position) {
        return (root, query, criteriaBuilder) -> {
            if (position == null || position.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("position")),
                    "%" + position.toLowerCase() + "%"
            );
        };
    }

    /**
     * Create a specification to filter VacancyResponse entities by interview stage.
     *
     * @param stage the interview stage to filter by
     * @return a specification for filtering by interview stage
     */
    public static Specification<VacancyResponse> byInterviewStage(InterviewStage stage) {
        return (root, query, criteriaBuilder) -> {
            if (stage == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("interviewStage"), stage);
        };
    }

    /**
     * Create a specification to search VacancyResponse entities by query and interview stage.
     * This replaces the custom JPQL query in the repository.
     *
     * @param query the search query for company name, position, or technologies
     * @param stage the interview stage to filter by
     * @return a specification for searching by query and interview stage
     */
    public static Specification<VacancyResponse> searchVacancyResponses(String query, InterviewStage stage) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Predicate queryPredicate = searchByQuery(query).toPredicate(root, criteriaQuery, criteriaBuilder);
            Predicate stagePredicate = byInterviewStage(stage).toPredicate(root, criteriaQuery, criteriaBuilder);
            return criteriaBuilder.and(queryPredicate, stagePredicate);
        };
    }

    /**
     * Create a specification to search VacancyResponse entities by query.
     * This searches in company name, position, and technologies fields.
     *
     * @param query the search query
     * @return a specification for searching by query
     */
    private static Specification<VacancyResponse> searchByQuery(String query) {
        return (vacancyResponseRoot, criteriaQuery, criteriaBuilder) -> {
            if (query == null || query.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String likePattern = "%" + query.toLowerCase() + "%";

            Join<VacancyResponse, Vacancy> vacancyJoin = vacancyResponseRoot.join("vacancy", JoinType.LEFT);

            Predicate byVacancyLocation = criteriaBuilder.like(criteriaBuilder.lower(vacancyResponseRoot.get("location")), likePattern);
            Predicate byCompanyName = criteriaBuilder.like(criteriaBuilder.lower(vacancyJoin.get("companyName")), likePattern);
            Predicate byPosition = criteriaBuilder.like(criteriaBuilder.lower(vacancyJoin.get("position")), likePattern);
            Predicate byTechnologies = criteriaBuilder.like(criteriaBuilder.lower(vacancyJoin.get("technologies")), likePattern);

            return criteriaBuilder.or(byTechnologies, byPosition, byCompanyName, byVacancyLocation);
        };
    }
}

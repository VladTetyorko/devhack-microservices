package com.vladte.devhack.domain.repository.global;

import com.vladte.devhack.domain.entities.global.InterviewStageCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for accessing and manipulating InterviewStageCategory entities.
 */
@Repository
public interface InterviewStageCategoryRepository extends JpaRepository<InterviewStageCategory, UUID> {

    /**
     * Find interview stage category by code.
     *
     * @param code the category code
     * @return optional interview stage category
     */
    Optional<InterviewStageCategory> findByCode(String code);

    Optional<InterviewStageCategory> findFirstByOrderIndexOrderByOrderIndex(Integer orderIndex);

    Optional<InterviewStageCategory> findFirstByOrderIndexGreaterThanOrderByOrderIndex(Integer orderIndex);

    Optional<InterviewStageCategory> findFirstByOrderIndexLessThanOrderByOrderIndex(Integer orderIndex);

}
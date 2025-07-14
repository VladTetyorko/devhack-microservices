package com.vladte.devhack.common.repository;

import com.vladte.devhack.entities.InterviewStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for accessing and manipulating InterviewStage entities.
 */
@Repository
public interface InterviewStageRepository extends JpaRepository<InterviewStage, UUID> {

    /**
     * Find interview stage by code.
     *
     * @param code the stage code
     * @return optional interview stage
     */
    Optional<InterviewStage> findByCode(String code);

    /**
     * Find all active interview stages ordered by order index.
     *
     * @return list of active interview stages
     */
    @Query("SELECT s FROM InterviewStage s WHERE s.active = true ORDER BY s.orderIndex ASC")
    List<InterviewStage> findAllActiveOrderByOrderIndex();

    /**
     * Find interview stages by category code.
     *
     * @param categoryCode the category code
     * @return list of interview stages
     */
    @Query("SELECT s FROM InterviewStage s WHERE s.category.code = :categoryCode ORDER BY s.orderIndex ASC")
    List<InterviewStage> findByCategoryCode(@Param("categoryCode") String categoryCode);

    /**
     * Find all final stages.
     *
     * @return list of final interview stages
     */
    @Query("SELECT s FROM InterviewStage s WHERE s.finalStage = true ORDER BY s.orderIndex ASC")
    List<InterviewStage> findAllFinalStages();

    /**
     * Find all non-internal stages (visible to candidates).
     *
     * @return list of non-internal interview stages
     */
    @Query("SELECT s FROM InterviewStage s WHERE s.internalOnly = false OR s.internalOnly IS NULL ORDER BY s.orderIndex ASC")
    List<InterviewStage> findAllNonInternalStages();

    /**
     * Find first interview stage ordered by order index.
     *
     * @return optional interview stage with lowest order index
     */
    Optional<InterviewStage> findFirstByOrderByOrderIndexAsc();

}
package com.vladte.devhack.domain.service.global;

import com.vladte.devhack.domain.entities.global.InterviewStage;
import com.vladte.devhack.domain.service.CrudService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for InterviewStage entity operations.
 */
public interface InterviewStageService extends CrudService<InterviewStage, UUID> {

    /**
     * Find interview stage by code.
     *
     * @param code the stage code
     * @return optional interview stage
     */
    Optional<InterviewStage> findByCode(String code);

    /**
     * Find the first stage in the interview process.
     *
     * @return optional first interview stage
     */
    Optional<InterviewStage> findFirstStage();

    Optional<InterviewStage> findRejectedStage();

    /**
     * Find all active interview stages ordered by order index.
     *
     * @return list of active interview stages
     */
    List<InterviewStage> findAllActiveOrderByOrderIndex();

    /**
     * Find interview stages by category code.
     *
     * @param categoryCode the category code
     * @return list of interview stages
     */
    List<InterviewStage> findByCategoryCode(String categoryCode);

    /**
     * Find all final stages.
     *
     * @return list of final interview stages
     */
    List<InterviewStage> findAllFinalStages();

    /**
     * Find all non-internal stages (visible to candidates).
     *
     * @return list of non-internal interview stages
     */
    List<InterviewStage> findAllNonInternalStages();

    /**
     * Get the next stage in the interview process.
     *
     * @param currentStage the current stage
     * @return optional next stage
     */
    Optional<InterviewStage> getNextStage(InterviewStage currentStage);

    /**
     * Get the previous stage in the interview process.
     *
     * @param currentStage the current stage
     * @return optional previous stage
     */
    Optional<InterviewStage> getPreviousStage(InterviewStage currentStage);

}
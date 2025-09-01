package com.vladte.devhack.domain.service.global;

import com.vladte.devhack.domain.entities.global.InterviewStageCategory;
import com.vladte.devhack.domain.service.CrudService;

import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for InterviewStageCategory entity operations.
 */
public interface InterviewStageCategoryService extends CrudService<InterviewStageCategory, UUID> {

    /**
     * Find interview stage category by code.
     *
     * @param code the category code
     * @return optional interview stage category
     */
    Optional<InterviewStageCategory> findByCode(String code);
}
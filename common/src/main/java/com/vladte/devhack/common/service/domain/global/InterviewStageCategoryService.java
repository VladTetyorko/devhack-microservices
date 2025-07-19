package com.vladte.devhack.common.service.domain.global;

import com.vladte.devhack.common.service.domain.CrudService;
import com.vladte.devhack.entities.InterviewStageCategory;

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
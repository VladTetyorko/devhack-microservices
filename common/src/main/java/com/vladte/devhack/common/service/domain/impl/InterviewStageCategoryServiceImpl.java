package com.vladte.devhack.common.service.domain.impl;

import com.vladte.devhack.common.repository.InterviewStageCategoryRepository;
import com.vladte.devhack.common.service.domain.AuditService;
import com.vladte.devhack.common.service.domain.InterviewStageCategoryService;
import com.vladte.devhack.entities.InterviewStageCategory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the InterviewStageCategoryService interface.
 */
@Service
public class InterviewStageCategoryServiceImpl extends BaseServiceImpl<InterviewStageCategory, UUID, InterviewStageCategoryRepository>
        implements InterviewStageCategoryService {

    /**
     * Constructor with repository injection.
     *
     * @param repository   the interview stage category repository
     * @param auditService the audit service
     */
    public InterviewStageCategoryServiceImpl(InterviewStageCategoryRepository repository, AuditService auditService) {
        super(repository, auditService);
    }

    @Override
    public Optional<InterviewStageCategory> findByCode(String code) {
        return repository.findByCode(code);
    }

    public Optional<InterviewStageCategory> findFirstCategory() {
        return repository.findFirstByOrderIndexOrderByOrderIndex(0);
    }

    public Optional<InterviewStageCategory> getNextCategory(InterviewStageCategory currentCategory) {
        return repository.findFirstByOrderIndexGreaterThanOrderByOrderIndex(currentCategory.getOrderIndex());
    }

    public Optional<InterviewStageCategory> getPreviousCategory(InterviewStageCategory currentCategory) {
        return repository.findFirstByOrderIndexLessThanOrderByOrderIndex(currentCategory.getOrderIndex());
    }
}
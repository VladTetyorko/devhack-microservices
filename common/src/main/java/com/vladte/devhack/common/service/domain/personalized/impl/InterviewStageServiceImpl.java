package com.vladte.devhack.common.service.domain.personalized.impl;

import com.vladte.devhack.common.repository.InterviewStageRepository;
import com.vladte.devhack.common.service.domain.audit.AuditService;
import com.vladte.devhack.common.service.domain.personalized.InterviewStageService;
import com.vladte.devhack.common.service.domain.AuditableCrudService;
import com.vladte.devhack.entities.InterviewStage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the InterviewStageService interface.
 */
@Service
public class InterviewStageServiceImpl extends AuditableCrudService<InterviewStage, UUID, InterviewStageRepository>
        implements InterviewStageService {

    /**
     * Constructor with repository injection.
     *
     * @param repository   the interview stage repository
     * @param auditService the audit service
     */

    public InterviewStageServiceImpl(InterviewStageRepository repository, AuditService auditService) {
        super(repository, auditService);
    }

    @Override
    public Optional<InterviewStage> findByCode(String code) {
        return repository.findByCode(code);
    }

    @Override
    public Optional<InterviewStage> findFirstStage() {
        return repository.findFirstByOrderByOrderIndexAsc();
    }

    @Override
    public Optional<InterviewStage> findRejectedStage() {
        return repository.findByCode("REJECTED");
    }

    @Override
    public List<InterviewStage> findAllActiveOrderByOrderIndex() {
        return repository.findAllActiveOrderByOrderIndex();
    }

    @Override
    public List<InterviewStage> findByCategoryCode(String categoryCode) {
        return repository.findByCategoryCode(categoryCode);
    }

    @Override
    public List<InterviewStage> findAllFinalStages() {
        return repository.findAllFinalStages();
    }

    @Override
    public List<InterviewStage> findAllNonInternalStages() {
        return repository.findAllNonInternalStages();
    }

    @Override
    public Optional<InterviewStage> getNextStage(InterviewStage currentStage) {
        List<InterviewStage> activeStages = findAllActiveOrderByOrderIndex();

        for (int i = 0; i < activeStages.size() - 1; i++) {
            if (activeStages.get(i).getId().equals(currentStage.getId())) {
                return Optional.of(activeStages.get(i + 1));
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<InterviewStage> getPreviousStage(InterviewStage currentStage) {
        List<InterviewStage> activeStages = findAllActiveOrderByOrderIndex();

        for (int i = 1; i < activeStages.size(); i++) {
            if (activeStages.get(i).getId().equals(currentStage.getId())) {
                return Optional.of(activeStages.get(i - 1));
            }
        }

        return Optional.empty();
    }
}
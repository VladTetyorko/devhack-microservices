package com.vladte.devhack.common.service.domain.impl;

import com.vladte.devhack.common.repository.VacancyResponseRepository;
import com.vladte.devhack.common.repository.specification.VacancyResponseSpecification;
import com.vladte.devhack.common.service.domain.VacancyResponseService;
import com.vladte.devhack.entities.InterviewStage;
import com.vladte.devhack.entities.User;
import com.vladte.devhack.entities.VacancyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementation of the VacancyResponseService interface.
 */
@Service
public class VacancyResponseServiceImpl extends UserOwnedServiceImpl<VacancyResponse, UUID, VacancyResponseRepository> implements VacancyResponseService {

    /**
     * Constructor with repository injection.
     *
     * @param repository the vacancy response repository
     */
    @Autowired
    public VacancyResponseServiceImpl(VacancyResponseRepository repository) {
        super(repository);
    }

    @Override
    public Page<VacancyResponse> getVacancyResponsesByUser(User user, Pageable pageable) {
        return repository.findAll(VacancyResponseSpecification.byUser(user), pageable);
    }

    @Override
    public Page<VacancyResponse> searchVacancyResponses(String query, InterviewStage stage, Pageable pageable) {
        return repository.findAll(
                VacancyResponseSpecification.searchVacancyResponses(query, stage),
                pageable
        );
    }

    @Override
    protected User getEntityUser(VacancyResponse entity) {
        return entity.getUser();
    }
}

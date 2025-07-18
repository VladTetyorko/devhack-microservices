package com.vladte.devhack.common.service.domain.impl;

import com.vladte.devhack.common.repository.VacancyResponseRepository;
import com.vladte.devhack.common.repository.specification.VacancyResponseSpecification;
import com.vladte.devhack.common.service.domain.InterviewStageService;
import com.vladte.devhack.common.service.domain.VacancyResponseService;
import com.vladte.devhack.entities.InterviewStage;
import com.vladte.devhack.entities.User;
import com.vladte.devhack.entities.Vacancy;
import com.vladte.devhack.entities.VacancyResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the VacancyResponseService interface.
 */
@Service
public class VacancyResponseServiceImpl extends UserOwnedServiceImpl<VacancyResponse, UUID, VacancyResponseRepository> implements VacancyResponseService {

    private final InterviewStageService interviewStageService;

    /**
     * Constructor with repository injection.
     *
     * @param repository            the vacancy response repository
     * @param interviewStageService the interview stage service
     */

    public VacancyResponseServiceImpl(VacancyResponseRepository repository, InterviewStageService interviewStageService) {
        super(repository);
        this.interviewStageService = interviewStageService;
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
    public VacancyResponse saveNewResponseForUserAndVacancy(User user, Vacancy vacancy) {
        VacancyResponse vacancyResponse = new VacancyResponse();
        vacancyResponse.setUser(user);
        vacancyResponse.setVacancy(vacancy);
        vacancyResponse.setInterviewStage(interviewStageService.findFirstStage().orElseThrow());
        return save(vacancyResponse);
    }

    @Override
    public List<VacancyResponse> getVacancyResponsesByVacancy(Vacancy vacancy) {
        return repository.findAllByVacancy(vacancy);
    }

    @Override
    @Cacheable(value = "vacancyResponsesByUser", key = "#user.id")
    public List<VacancyResponse> findVacancyResponsesByStageForUser(User user, Integer stageIndex) {
        return repository.findVacancyResponsesByUserAndInterviewStage_OrderIndex(user, stageIndex);
    }

    @Override
    protected User getEntityUser(VacancyResponse entity) {
        return entity.getUser();
    }
}

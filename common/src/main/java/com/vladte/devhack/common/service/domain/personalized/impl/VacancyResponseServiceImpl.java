package com.vladte.devhack.common.service.domain.personalized.impl;

import com.vladte.devhack.common.repository.VacancyResponseRepository;
import com.vladte.devhack.common.repository.specification.VacancyResponseSpecification;
import com.vladte.devhack.common.service.domain.audit.AuditService;
import com.vladte.devhack.common.service.domain.personalized.InterviewStageService;
import com.vladte.devhack.common.service.domain.personalized.VacancyResponseService;
import com.vladte.devhack.common.service.domain.personalized.PersonalizedService;
import com.vladte.devhack.entities.InterviewStage;
import com.vladte.devhack.entities.User;
import com.vladte.devhack.entities.Vacancy;
import com.vladte.devhack.entities.VacancyResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the VacancyResponseService interface.
 */
@Service
public class VacancyResponseServiceImpl extends PersonalizedService<VacancyResponse, UUID, VacancyResponseRepository> implements VacancyResponseService {

    private final InterviewStageService interviewStageService;

    private final static Integer INTERVIEW_STAGE_APPLIED_ORDER = 0;
    
    /**
     * Constructor with repository injection.
     *
     * @param repository            the vacancy response repository
     * @param interviewStageService the interview stage service
     */

    public VacancyResponseServiceImpl(VacancyResponseRepository repository, AuditService auditService, InterviewStageService interviewStageService) {
        super(repository, auditService);
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
    @Transactional
    public void markOutdatedResponses(User user) {
        InterviewStage rejected = interviewStageService.findRejectedStage().orElseThrow();
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);

        repository.bulkMarkOutdated(
                user,
                rejected,
                "The interview stage was changed to REJECTED; response marked as outdated by the user.",
                INTERVIEW_STAGE_APPLIED_ORDER,
                threshold
        );
    }


    @Override
    protected User getEntityUser(VacancyResponse entity) {
        return entity.getUser();
    }
}

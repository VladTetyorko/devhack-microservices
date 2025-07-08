package com.vladte.devhack.common.service.domain;

import com.vladte.devhack.entities.InterviewStage;
import com.vladte.devhack.entities.User;
import com.vladte.devhack.entities.Vacancy;
import com.vladte.devhack.entities.VacancyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing VacancyResponse entities.
 */
public interface VacancyResponseService extends BaseService<VacancyResponse, UUID> {

    /**
     * Get all vacancy responses for a specific user with pagination.
     *
     * @param user     the user to get vacancy responses for
     * @param pageable the pagination information
     * @return a page of vacancy responses
     */
    Page<VacancyResponse> getVacancyResponsesByUser(User user, Pageable pageable);

    /**
     * Search for vacancy responses by query and interview stage with pagination.
     *
     * @param query    the search query for company name, position, or technologies
     * @param stage    the interview stage to filter by
     * @param pageable the pagination information
     * @return a page of vacancy responses
     */
    Page<VacancyResponse> searchVacancyResponses(String query, InterviewStage stage, Pageable pageable);


    VacancyResponse saveNewResponseForUserAndVacancy(User user, Vacancy vacancy);

    List<VacancyResponse> getVacancyResponsesByVacancy(Vacancy vacancy);
}

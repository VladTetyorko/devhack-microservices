package com.vladte.devhack.common.service.domain.global;

import com.vladte.devhack.common.service.domain.CrudService;
import com.vladte.devhack.entities.Vacancy;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing Vacancy entities.
 */
public interface VacancyService extends CrudService<Vacancy, UUID> {

    /**
     * Find vacancies by company name.
     *
     * @param companyName The company name to search for
     * @return A list of vacancies from the specified company
     */
    List<Vacancy> findByCompanyName(String companyName);

    /**
     * Find vacancies by position.
     *
     * @param position The position to search for
     * @return A list of vacancies with the specified position
     */
    List<Vacancy> findByPosition(String position);

    /**
     * Find vacancies by source.
     *
     * @param source The source to search for
     * @return A list of vacancies from the specified source
     */
    List<Vacancy> findBySource(String source);

    /**
     * Find vacancies by remote allowed status.
     *
     * @param remoteAllowed The remote allowed status to search for
     * @return A list of vacancies with the specified remote allowed status
     */
    List<Vacancy> findByRemoteAllowed(Boolean remoteAllowed);

    /**
     * Search vacancies by keyword in company name, position, or technologies.
     *
     * @param keyword The keyword to search for
     * @return A list of vacancies matching the keyword
     */
    List<Vacancy> searchByKeyword(String keyword);
}
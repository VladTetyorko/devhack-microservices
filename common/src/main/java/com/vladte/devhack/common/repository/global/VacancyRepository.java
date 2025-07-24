package com.vladte.devhack.common.repository.global;

import com.vladte.devhack.entities.global.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for accessing and manipulating Vacancy entities.
 */
@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, UUID>, JpaSpecificationExecutor<Vacancy> {

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
     * Find vacancies by URL.
     *
     * @param url The URL to search for
     * @return A list of vacancies with the specified URL
     */
    List<Vacancy> findByUrl(String url);

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
    @Query("SELECT v FROM Vacancy v WHERE " +
            "LOWER(v.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.position) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.technologies) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Vacancy> searchByKeyword(@Param("keyword") String keyword);
}
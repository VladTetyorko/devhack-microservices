package com.vladte.devhack.parser.repository;

import com.vladte.devhack.entities.global.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Vacancy entities.
 */
@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, UUID> {

    /**
     * Find vacancies by company name.
     *
     * @param companyName The company name to search for
     * @return A list of vacancies from the specified company
     */
    List<Vacancy> findByCompanyName(String companyName);

    /**
     * Find vacancies by source.
     *
     * @param source The source to search for
     * @return A list of vacancies from the specified source
     */
    List<Vacancy> findBySource(String source);

    /**
     * Find a vacancy by its URL.
     */
    Optional<Vacancy> findByUrl(String url);

    @Query(value = "SELECT * from vacancies where vacancies.source = :provider order by created_at desc limit 1", nativeQuery = true)
    Optional<Vacancy> findLastSavedVacancyForProvider(String provider);
}
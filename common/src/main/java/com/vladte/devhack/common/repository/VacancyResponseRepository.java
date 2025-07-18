package com.vladte.devhack.common.repository;

import com.vladte.devhack.entities.User;
import com.vladte.devhack.entities.Vacancy;
import com.vladte.devhack.entities.VacancyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for accessing and manipulating VacancyResponse entities.
 */
@Repository
public interface VacancyResponseRepository extends JpaRepository<VacancyResponse, UUID>, JpaSpecificationExecutor<VacancyResponse> {
    List<VacancyResponse> findAllByVacancy(Vacancy vacancy);

    List<VacancyResponse> findVacancyResponsesByUserAndInterviewStage_OrderIndex(User user, Integer interviewStage_orderIndex);

}

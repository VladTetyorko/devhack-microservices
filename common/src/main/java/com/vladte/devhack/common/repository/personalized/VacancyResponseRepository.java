package com.vladte.devhack.common.repository.personalized;

import com.vladte.devhack.entities.global.InterviewStage;
import com.vladte.devhack.entities.global.Vacancy;
import com.vladte.devhack.entities.personalized.VacancyResponse;
import com.vladte.devhack.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for accessing and manipulating VacancyResponse entities.
 */
@Repository
public interface VacancyResponseRepository extends JpaRepository<VacancyResponse, UUID>, JpaSpecificationExecutor<VacancyResponse> {
    List<VacancyResponse> findAllByVacancy(Vacancy vacancy);

    List<VacancyResponse> findVacancyResponsesByUserAndInterviewStage_OrderIndex(User user, Integer interviewStage_orderIndex);

    // Repository
    @Modifying
    @Query("""
              UPDATE VacancyResponse v
                SET v.interviewStage = :rejected,
                    v.notes          = :notes,
                    v.updatedAt      = CURRENT_TIMESTAMP
              WHERE v.user            = :user
                AND v.interviewStage.orderIndex = :appliedOrder
                AND v.createdAt < :expiryDate
            """)
    void bulkMarkOutdated(
            @Param("user") User user,
            @Param("rejected") InterviewStage rejected,
            @Param("notes") String notes,
            @Param("appliedOrder") int appliedOrder,
            @Param("expiryDate") LocalDateTime expiryDate
    );


}

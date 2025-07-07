package com.vladte.devhack.common.dto;

import com.vladte.devhack.entities.VacancyStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Vacancy entity.
 * Implements BaseDTO for consistency with other DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacancyDTO implements BaseDTO {
    private UUID id;
    private String companyName;
    private String position;
    private String technologies;
    private String source;
    private String url;
    private LocalDateTime appliedAt;
    private VacancyStatus status;
    private String contactPerson;
    private String contactEmail;
    private LocalDateTime deadline;
    private Boolean remoteAllowed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int responseCount;
}
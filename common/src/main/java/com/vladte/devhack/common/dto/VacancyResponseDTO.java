package com.vladte.devhack.common.dto;

import com.vladte.devhack.entities.InterviewStage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for VacancyResponse entity.
 * Implements BaseDTO for consistency with other DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacancyResponseDTO implements BaseDTO {
    private UUID id;
    private UUID userId;
    private String userName;
    private UUID vacancyId;
    private String companyName;
    private String position;
    private String technologies;
    private String pros;
    private String cons;
    private String notes;
    private String salary;
    private String location;
    private InterviewStage interviewStage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<UUID> tagIds = new HashSet<>();
    private Set<String> tagNames = new HashSet<>();
}

package com.vladte.devhack.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Vacancy response data transfer object")
public class VacancyResponseDTO implements BaseDTO {

    @Schema(description = "Unique identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "ID of the user who created the response", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID userId;

    @Schema(description = "Name of the user who created the response", accessMode = Schema.AccessMode.READ_ONLY)
    private String userName;

    @NotNull(message = "Vacancy ID is required")
    @Schema(description = "ID of the vacancy this response is for", required = true)
    private UUID vacancyId;

    @Schema(description = "Company name from the vacancy", accessMode = Schema.AccessMode.READ_ONLY)
    private String companyName;

    @Schema(description = "Position from the vacancy", accessMode = Schema.AccessMode.READ_ONLY)
    private String position;

    @Schema(description = "Technologies required for the vacancy", accessMode = Schema.AccessMode.READ_ONLY)
    private String technologies;

    @Size(max = 1000, message = "Pros must be less than 1000 characters")
    @Schema(description = "Pros of the vacancy", example = "Good salary, remote work, interesting project")
    private String pros;

    @Size(max = 1000, message = "Cons must be less than 1000 characters")
    @Schema(description = "Cons of the vacancy", example = "Long commute, outdated technology stack")
    private String cons;

    @Size(max = 2000, message = "Notes must be less than 2000 characters")
    @Schema(description = "Additional notes about the vacancy", example = "Need to prepare for system design questions")
    private String notes;

    @Schema(description = "Salary offered", example = "$100,000 - $120,000")
    private String salary;

    @Schema(description = "Location of the job", example = "New York, NY")
    private String location;

    @NotNull(message = "Interview stage is required")
    @Schema(description = "Current stage of the interview process", required = true)
    private String interviewStageId;

    @NotNull(message = "Interview stage is required")
    @Schema(description = "Current stage of the interview process", required = true, example = "APPLIED")
    private String interviewStage;

    @Schema(description = "Interview stage details", accessMode = Schema.AccessMode.READ_ONLY)
    private InterviewStageDTO interviewStageDTO;

    @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Schema(description = "IDs of tags associated with this vacancy response", accessMode = Schema.AccessMode.READ_ONLY)
    private Set<UUID> tagIds = new HashSet<>();

    @Schema(description = "Names of tags associated with this vacancy response", accessMode = Schema.AccessMode.READ_ONLY)
    private Set<String> tagNames = new HashSet<>();
}

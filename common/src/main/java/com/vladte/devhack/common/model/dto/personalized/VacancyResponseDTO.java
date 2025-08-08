package com.vladte.devhack.common.model.dto.personalized;

import com.vladte.devhack.common.model.dto.UserOwnedDTO;
import com.vladte.devhack.common.model.dto.global.InterviewStageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for VacancyResponse entity.
 * Extends UserOwnedDTO for consistency with other user-owned DTOs.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Vacancy response data transfer object")
public class VacancyResponseDTO extends UserOwnedDTO {

    @NotNull(message = "Vacancy ID is required")
    @Schema(description = "ID of the vacancy this response is for", requiredMode = Schema.RequiredMode.AUTO)
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
    @Schema(description = "Current stage of the interview process", requiredMode = Schema.RequiredMode.AUTO)
    private String interviewStageId;

    @NotNull(message = "Interview stage is required")
    @Schema(description = "Current stage of the interview process", requiredMode = Schema.RequiredMode.AUTO, example = "APPLIED")
    private String interviewStage;

    @Schema(description = "Interview stage details", accessMode = Schema.AccessMode.READ_ONLY)
    private InterviewStageDTO interviewStageDTO;

    @Schema(description = "IDs of tags associated with this vacancy response", accessMode = Schema.AccessMode.READ_ONLY)
    private Set<UUID> tagIds = new HashSet<>();

    @Schema(description = "Names of tags associated with this vacancy response", accessMode = Schema.AccessMode.READ_ONLY)
    private Set<String> tagNames = new HashSet<>();
}

package com.vladte.devhack.domain.model.dto.global;

import com.vladte.devhack.domain.entities.enums.VacancyStatus;
import com.vladte.devhack.domain.model.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Vacancy data transfer object")
public class VacancyDTO implements BaseDTO {

    @Schema(description = "Unique identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    @Schema(description = "Company name", example = "Acme Inc.", requiredMode = Schema.RequiredMode.AUTO)
    private String companyName;

    @NotBlank(message = "Position is required")
    @Size(min = 2, max = 100, message = "Position must be between 2 and 100 characters")
    @Schema(description = "Job position", example = "Senior Java Developer", requiredMode = Schema.RequiredMode.AUTO)
    private String position;

    @Schema(description = "Required technologies", example = "Java, Spring Boot, PostgreSQL")
    private String technologies;

    @Schema(description = "Source of the vacancy", example = "LinkedIn")
    private String source;

    @Schema(description = "URL of the vacancy posting", example = "https://example.com/jobs/123")
    private String url;

    @Schema(description = "Date when vacancy was OPEN")
    private LocalDateTime openAt;

    @NotNull(message = "Status is required")
    @Schema(description = "Current status of the vacancy", requiredMode = Schema.RequiredMode.AUTO)
    private VacancyStatus status;

    @Schema(description = "Contact person name", example = "John Doe")
    private String contactPerson;

    @Email(message = "Invalid email format")
    @Schema(description = "Contact person email", example = "john.doe@example.com")
    private String contactEmail;

    @Schema(description = "Application deadline")
    private LocalDateTime deadline;

    @Schema(description = "Whether remote work is allowed", example = "true")
    private Boolean remoteAllowed;

    @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Schema(description = "Number of responses to this vacancy", accessMode = Schema.AccessMode.READ_ONLY)
    private int responseCount;

    @Schema(description = "Full vacancy description text")
    private String description;
}

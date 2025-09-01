package com.vladte.devhack.domain.model.dto.global;

import com.vladte.devhack.domain.model.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for InterviewStageCategory entity.
 * Implements BaseDTO for consistency with other DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Interview stage category data transfer object")
public class InterviewStageCategoryDTO implements BaseDTO {

    @Schema(description = "Unique identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotBlank(message = "Category code is required")
    @Size(min = 2, max = 50, message = "Category code must be between 2 and 50 characters")
    @Schema(description = "Category code", example = "TECHNICAL", requiredMode = Schema.RequiredMode.AUTO)
    private String code;

    @NotBlank(message = "Category label is required")
    @Size(min = 2, max = 100, message = "Category label must be between 2 and 100 characters")
    @Schema(description = "Category label", example = "Technical Assessment", requiredMode = Schema.RequiredMode.AUTO)
    private String label;

    @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}
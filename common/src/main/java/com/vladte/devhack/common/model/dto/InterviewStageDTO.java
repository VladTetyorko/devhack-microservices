package com.vladte.devhack.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for InterviewStage entity.
 * Implements BaseDTO for consistency with other DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Interview stage data transfer object")
public class InterviewStageDTO implements BaseDTO {

    @Schema(description = "Unique identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotBlank(message = "Stage code is required")
    @Size(min = 2, max = 50, message = "Stage code must be between 2 and 50 characters")
    @Schema(description = "Stage code", example = "TECHNICAL_INTERVIEW", requiredMode = Schema.RequiredMode.AUTO)
    private String code;

    @NotBlank(message = "Stage label is required")
    @Size(min = 2, max = 100, message = "Stage label must be between 2 and 100 characters")
    @Schema(description = "Stage label", example = "Technical Interview", requiredMode = Schema.RequiredMode.AUTO)
    private String label;

    @Schema(description = "Sort order on Kanban board", example = "4")
    private Integer orderIndex;

    @Size(max = 30, message = "Color class must not exceed 30 characters")
    @Schema(description = "CSS color class", example = "primary")
    private String colorClass;

    @Size(max = 50, message = "Icon class must not exceed 50 characters")
    @Schema(description = "FontAwesome icon class", example = "fa-code")
    private String iconClass;

    @Schema(description = "Whether the stage is active", example = "true")
    private Boolean active;

    @Schema(description = "Whether this is a final stage", example = "false")
    private Boolean finalStage;

    @Schema(description = "Whether this stage is internal only", example = "false")
    private Boolean internalOnly;

    @NotNull(message = "Category is required")
    @Schema(description = "Category ID", requiredMode = Schema.RequiredMode.AUTO)
    private UUID categoryId;

    @Schema(description = "Category information", accessMode = Schema.AccessMode.READ_ONLY)
    private InterviewStageCategoryDTO category;

    @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}
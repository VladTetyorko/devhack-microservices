package com.vladte.devhack.common.model.dto.global.ai;

import com.vladte.devhack.common.model.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for AiPrompt entity.
 * Implements BaseDTO for consistency with other DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI prompt data")
public class AiPromptDTO implements BaseDTO {

    @Schema(description = "Unique identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Unique prompt code", example = "QUESTION_GENERATION", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "Prompt description", example = "Generate interview questions")
    private String description;

    @Schema(description = "The actual prompt text", requiredMode = Schema.RequiredMode.REQUIRED)
    private String prompt;

    @Schema(description = "Language code", example = "en")
    private String language;

    @Schema(description = "Is prompt active?", example = "true")
    private Boolean active;

    @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Schema(description = "Category ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID categoryId;

    @Schema(description = "Category name", accessMode = Schema.AccessMode.READ_ONLY)
    private String categoryName;
}
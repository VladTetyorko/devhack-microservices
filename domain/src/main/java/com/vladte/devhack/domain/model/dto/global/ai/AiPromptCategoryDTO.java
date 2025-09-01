package com.vladte.devhack.domain.model.dto.global.ai;

import com.vladte.devhack.domain.model.dto.BaseTimestampedDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for AiPromptCategory entity.
 * Extends BaseTimestampedDTO for consistency with other timestamped DTOs.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI prompt category data")
public class AiPromptCategoryDTO extends BaseTimestampedDTO {

    @Schema(description = "Unique category code", example = "INTERVIEW_QUESTIONS", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "Category description", example = "Prompts for generating interview questions")
    private String description;

    @Schema(description = "Category name", example = "Interview Questions", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "List of prompts in this category", accessMode = Schema.AccessMode.READ_ONLY)
    private List<AiPromptDTO> prompts = new ArrayList<>();

    @Schema(description = "Number of prompts in this category", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer promptCount;
}

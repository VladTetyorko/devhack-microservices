package com.vladte.devhack.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for Tag entity.
 * Implements BaseDTO for consistency with other DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tag data transfer object")
public class TagDTO implements BaseDTO {

    @Schema(description = "Unique identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotBlank(message = "Tag name is required")
    @Size(min = 2, max = 50, message = "Tag name must be between 2 and 50 characters")
    @Schema(description = "Tag name", example = "Java", requiredMode = Schema.RequiredMode.AUTO)
    private String name;

    @Schema(description = "IDs of questions associated with this tag", accessMode = Schema.AccessMode.READ_ONLY)
    private Set<UUID> questionIds = new HashSet<>();

    @Schema(description = "Number of answered questions with this tag", accessMode = Schema.AccessMode.READ_ONLY)
    private int answeredQuestions;

    @Schema(description = "Percentage of questions answered with this tag", example = "75.5", accessMode = Schema.AccessMode.READ_ONLY)
    private double progressPercentage;

    @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    /**
     * Generate a URL-friendly slug from the tag name.
     * Converts to lowercase and replaces spaces with hyphens.
     *
     * @return the slug
     */
    public String getSlug() {
        if (name == null) {
            return "";
        }
        return name.toLowerCase().replace(' ', '-');
    }
}

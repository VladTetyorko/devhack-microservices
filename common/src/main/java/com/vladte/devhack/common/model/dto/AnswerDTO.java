package com.vladte.devhack.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Answer entity.
 * Implements BaseDTO for consistency with other DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Answer data transfer object")
public class AnswerDTO implements BaseDTO {

    @Schema(description = "Unique identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotBlank(message = "Answer text is required")
    @Size(min = 5, max = 5000, message = "Answer text must be between 5 and 5000 characters")
    @Schema(description = "Text of the answer", example = "SOLID principles are: Single Responsibility, Open-Closed, Liskov Substitution, Interface Segregation, and Dependency Inversion", required = true)
    private String text;

    @Min(value = 1, message = "Confidence level must be at least 1")
    @Max(value = 10, message = "Confidence level must be at most 10")
    @Schema(description = "User's confidence level in the answer (1-10)", example = "8")
    private Integer confidenceLevel;

    @Schema(description = "AI-generated score for the answer", example = "0.85", accessMode = Schema.AccessMode.READ_ONLY)
    private Double aiScore;

    @Schema(description = "AI-generated feedback for the answer", accessMode = Schema.AccessMode.READ_ONLY)
    private String aiFeedback;

    @Schema(description = "Whether the answer is marked as correct", example = "true", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean isCorrect;

    @Schema(description = "Whether the answer is flagged as cheating", example = "false", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean isCheating;

    @Schema(description = "Last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Schema(description = "ID of the user who provided the answer", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID userId;

    @Schema(description = "Name of the user who provided the answer", accessMode = Schema.AccessMode.READ_ONLY)
    private String userName;

    @Schema(description = "ID of the question being answered", required = true)
    private UUID questionId;

    @Schema(description = "Text of the question being answered", accessMode = Schema.AccessMode.READ_ONLY)
    private String questionText;

    @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}

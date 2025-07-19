package com.vladte.devhack.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

/**
 * DTO for InterviewQuestion entity.
 * Implements BaseDTO for consistency with other DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Interview question data transfer object")
public class InterviewQuestionDTO implements BaseDTO {

    @Schema(description = "Unique identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotBlank(message = "Question text is required")
    @Size(min = 10, max = 2000, message = "Question text must be between 10 and 2000 characters")
    @Schema(description = "Text of the interview question", example = "What are the principles of SOLID?", required = true)
    private String questionText;

    @NotBlank(message = "Difficulty is required")
    @Schema(description = "Difficulty level of the question", example = "MEDIUM", required = true)
    private String difficulty;

    @Schema(description = "Source of the question", example = "Java Interview Book")
    private String source;

    @Schema(description = "ID of the user who created the question", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID userId;

    @Schema(description = "Name of the user who created the question", accessMode = Schema.AccessMode.READ_ONLY)
    private String userName;

    @Schema(description = "IDs of tags associated with this question", accessMode = Schema.AccessMode.READ_ONLY)
    private Set<TagDTO> tags = new HashSet<>();

    @Schema(description = "IDs of answers to this question", accessMode = Schema.AccessMode.READ_ONLY)
    private List<AnswerDTO> answers = new ArrayList<>();

    @Schema(description = "IDs of notes related to this question", accessMode = Schema.AccessMode.READ_ONLY)
    private List<NoteDTO> notes = new ArrayList<>();

    @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}

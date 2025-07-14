package com.vladte.devhack.common.dto;

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
 * DTO for Note entity.
 * Implements BaseDTO for consistency with other DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Note data transfer object")
public class NoteDTO implements BaseDTO {

    @Schema(description = "Unique identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "ID of the user who created the note", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID userId;

    @Schema(description = "Name of the user who created the note", accessMode = Schema.AccessMode.READ_ONLY)
    private String userName;

    @NotNull(message = "Question ID is required")
    @Schema(description = "ID of the question this note is related to", required = true)
    private UUID questionId;

    @Schema(description = "Text of the question this note is related to", accessMode = Schema.AccessMode.READ_ONLY)
    private String questionText;

    @NotBlank(message = "Note text is required")
    @Size(min = 5, max = 2000, message = "Note text must be between 5 and 2000 characters")
    @Schema(description = "Text content of the note", example = "This question is about design patterns. Remember to mention Factory, Singleton, and Observer patterns.", required = true)
    private String noteText;

    @Schema(description = "Last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}

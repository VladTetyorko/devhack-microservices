package com.vladte.devhack.domain.model.dto.personalized;

import com.vladte.devhack.domain.model.dto.UserOwnedDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for Note entity.
 * Extends UserOwnedDTO for consistency with other user-owned DTOs.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Note data transfer object")
public class NoteDTO extends UserOwnedDTO {

    @NotNull(message = "Question ID is required")
    @Schema(description = "ID of the question this note is related to", requiredMode = Schema.RequiredMode.AUTO)
    private UUID questionId;

    @Schema(description = "Text of the question this note is related to", accessMode = Schema.AccessMode.READ_ONLY)
    private String questionText;

    @NotBlank(message = "Note text is required")
    @Size(min = 5, max = 2000, message = "Note text must be between 5 and 2000 characters")
    @Schema(description = "Text content of the note", example = "This question is about design patterns. Remember to mention Factory, Singleton, and Observer patterns.", requiredMode = Schema.RequiredMode.AUTO)
    private String noteText;
}

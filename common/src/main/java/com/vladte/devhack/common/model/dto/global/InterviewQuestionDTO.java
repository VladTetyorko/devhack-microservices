package com.vladte.devhack.common.model.dto.global;

import com.vladte.devhack.common.model.dto.UserOwnedDTO;
import com.vladte.devhack.common.model.dto.personalized.AnswerDTO;
import com.vladte.devhack.common.model.dto.personalized.NoteDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DTO for InterviewQuestion entity.
 * Extends UserOwnedDTO for consistency with other user-owned DTOs.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Interview question data transfer object")
public class InterviewQuestionDTO extends UserOwnedDTO {

    @NotBlank(message = "Question text is required")
    @Size(min = 10, max = 2000, message = "Question text must be between 10 and 2000 characters")
    @Schema(description = "Text of the interview question", example = "What are the principles of SOLID?", requiredMode = Schema.RequiredMode.AUTO)
    private String questionText;

    @NotBlank(message = "Difficulty is required")
    @Schema(description = "Difficulty level of the question", example = "MEDIUM", requiredMode = Schema.RequiredMode.AUTO)
    private String difficulty;

    @Schema(description = "Source of the question", example = "Java Interview Book")
    private String source;

    @Schema(description = "IDs of tags associated with this question", accessMode = Schema.AccessMode.READ_ONLY)
    private Set<TagDTO> tags = new HashSet<>();

    @Schema(description = "IDs of answers to this question", accessMode = Schema.AccessMode.READ_ONLY)
    private List<AnswerDTO> answers = new ArrayList<>();

    @Schema(description = "IDs of notes related to this question", accessMode = Schema.AccessMode.READ_ONLY)
    private List<NoteDTO> notes = new ArrayList<>();
}

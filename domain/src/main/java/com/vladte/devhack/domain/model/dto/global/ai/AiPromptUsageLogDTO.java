package com.vladte.devhack.domain.model.dto.global.ai;

import com.vladte.devhack.domain.model.dto.UserOwnedDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for AiPromptUsageLog entity.
 * Extends UserOwnedDTO for consistency with other user-owned DTOs.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI prompt usage log data")
public class AiPromptUsageLogDTO extends UserOwnedDTO {

    @Schema(description = "Prompt ID that was used", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID promptId;

    @Schema(description = "Prompt code that was used", accessMode = Schema.AccessMode.READ_ONLY)
    private String promptCode;

    @Schema(description = "Input provided to the AI prompt")
    private String input;

    @Schema(description = "Result returned by the AI", accessMode = Schema.AccessMode.READ_ONLY)
    private String result;
}

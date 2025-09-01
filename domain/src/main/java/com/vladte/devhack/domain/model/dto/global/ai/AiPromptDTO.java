package com.vladte.devhack.domain.model.dto.global.ai;

import com.vladte.devhack.domain.model.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI prompt data (new schema)")
public class AiPromptDTO implements BaseDTO {

    @Schema(description = "Unique identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Unique prompt key", example = "check_answer_feedback", requiredMode = Schema.RequiredMode.REQUIRED)
    private String key;

    @Schema(description = "System template text")
    private String systemTemplate;

    @Schema(description = "User template text", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userTemplate;

    @Schema(description = "Enabled status", example = "true")
    private Boolean enabled;

    @Schema(description = "Arguments JSON schema (object)")
    private Map<String, Object> argsSchema;

    @Schema(description = "Default parameter values (object)")
    private Map<String, Object> defaults;

    @Schema(description = "Model identifier", example = "gpt-3.5-turbo")
    private String model;

    @Schema(description = "Model parameters (object)")
    private Map<String, Object> parameters;

    @Schema(description = "Response contract schema (object)")
    private Map<String, Object> responseContract;

    @Schema(description = "Version", example = "1")
    private Integer version;

    @Schema(description = "Prompt description")
    private String description;

    @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Schema(description = "Category ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID categoryId;

    @Schema(description = "Category name", accessMode = Schema.AccessMode.READ_ONLY)
    private String categoryName;
}
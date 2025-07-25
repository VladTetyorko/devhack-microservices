package com.vladte.devhack.common.model.dto.user;

import com.vladte.devhack.common.model.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile data")
public class ProfileDTO implements BaseDTO {

    @Schema(description = "Unique identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Display name", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "CV file URL", accessMode = Schema.AccessMode.READ_ONLY)
    private String cvFileHref;

    @Schema(description = "CV file name", accessMode = Schema.AccessMode.READ_ONLY)
    private String cvFileName;

    @Schema(description = "CV MIME type", accessMode = Schema.AccessMode.READ_ONLY)
    private String cvFileType;

    @Schema(description = "CV file size in bytes", accessMode = Schema.AccessMode.READ_ONLY)
    private Long cvFileSize;

    @Schema(description = "CV upload timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime cvUploadedAt;

    @Schema(description = "Was CV parsed successfully?", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean cvParsedSuccessfully;

    @Schema(description = "Is AI usage enabled?", example = "false")
    private Boolean aiUsageEnabled;

    @Schema(description = "Preferred language for AI", example = "en")
    private String aiPreferredLanguage;

    @Schema(description = "AI-generated CV score", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer aiCvScore;

    @Schema(description = "AI-generated skills summary", accessMode = Schema.AccessMode.READ_ONLY)
    private String aiSkillsSummary;

    @Schema(description = "AI-suggested improvements", accessMode = Schema.AccessMode.READ_ONLY)
    private String aiSuggestedImprovements;

    @Schema(description = "Profile visibility to recruiters", example = "true")
    private Boolean isVisibleToRecruiters;
}

package com.vladte.devhack.domain.model.dto.user;

import com.vladte.devhack.domain.model.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication provider credentials (LOCAL or SOCIAL)")
public class AuthenticationProviderDTO implements BaseDTO {

    @Schema(description = "Unique identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Provider type", example = "LOCAL", requiredMode = Schema.RequiredMode.AUTO)
    private String provider;

    @Schema(description = "Provider-specific user ID (e.g. Google sub)")
    private String providerUserId;

    @Schema(description = "Email (for LOCAL login)", example = "user@example.com")
    private String email;

    @Schema(description = "OAuth access token (if applicable)", accessMode = Schema.AccessMode.READ_ONLY)
    private String accessToken;

    @Schema(description = "OAuth refresh token (if applicable)", accessMode = Schema.AccessMode.READ_ONLY)
    private String refreshToken;

    @Schema(description = "Token expiry timestamp (if applicable)", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime tokenExpiry;
}

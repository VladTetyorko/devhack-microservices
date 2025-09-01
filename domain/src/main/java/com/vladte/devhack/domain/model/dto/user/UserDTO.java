package com.vladte.devhack.domain.model.dto.user;

import com.vladte.devhack.domain.model.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Aggregated user data, including credentials, profile, and access")
public class UserDTO implements BaseDTO {

    @Schema(description = "Unique identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Account creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "IDs of authentication providers (LOCAL & SOCIAL)", accessMode = Schema.AccessMode.READ_ONLY)
    private List<UUID> credentialIds = new ArrayList<>();

    @Schema(description = "ID of user profile data", accessMode = Schema.AccessMode.READ_WRITE)
    private UUID profileId;

    @Schema(description = "ID of admin settings and role", accessMode = Schema.AccessMode.READ_WRITE)
    private UUID accessId;
}

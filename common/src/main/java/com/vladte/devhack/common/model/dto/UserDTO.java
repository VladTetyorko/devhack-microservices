// UserDTO.java
package com.vladte.devhack.common.model.dto;

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

    @Schema(description = "Authentication providers (LOCAL & SOCIAL)", accessMode = Schema.AccessMode.READ_ONLY)
    private List<AuthenticationProviderDTO> credentials = new ArrayList<>();

    @Schema(description = "User profile data", accessMode = Schema.AccessMode.READ_WRITE)
    private ProfileDTO profile;

    @Schema(description = "Admin settings and role", accessMode = Schema.AccessMode.READ_WRITE)
    private UserAccessDTO access;
}

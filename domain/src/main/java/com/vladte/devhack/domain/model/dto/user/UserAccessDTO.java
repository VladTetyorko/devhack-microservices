package com.vladte.devhack.domain.model.dto.user;

import com.vladte.devhack.domain.model.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin settings and role for a user")
public class UserAccessDTO implements BaseDTO {

    @Schema(description = "Unique identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Role name", example = "ADMIN", requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    @Schema(description = "Is AI usage allowed for this role?", example = "true")
    private Boolean aiUsageAllowed;

    @Schema(description = "Is account locked?", example = "false")
    private Boolean accountLocked;
}

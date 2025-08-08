package com.vladte.devhack.common.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for password reset confirmation.
 * Contains token and new password for password reset completion.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Password reset confirmation containing token and new password")
public class PasswordResetConfirmDTO implements Serializable {

    @NotBlank(message = "Reset token is required")
    @Schema(description = "Password reset token", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Schema(description = "New password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    @Schema(description = "New password confirmation", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;

    /**
     * Check if passwords match.
     *
     * @return true if passwords match, false otherwise
     */
    public boolean isPasswordsMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
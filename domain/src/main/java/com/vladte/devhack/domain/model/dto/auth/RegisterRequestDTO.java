package com.vladte.devhack.domain.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for user registration requests.
 * Contains user information needed for account creation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registration request containing user information")
public class RegisterRequestDTO implements Serializable {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    @Schema(description = "User's email address", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Schema(description = "User's password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "Password confirmation is required")
    @Schema(description = "Password confirmation", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be less than 50 characters")
    @Schema(description = "User's first name", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be less than 50 characters")
    @Schema(description = "User's last name", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @Size(max = 20, message = "Phone number must be less than 20 characters")
    @Schema(description = "User's phone number", example = "+1234567890")
    private String phoneNumber;

    @Schema(description = "Accept terms and conditions", example = "true")
    private boolean acceptTerms = false;

    /**
     * Check if passwords match.
     *
     * @return true if passwords match, false otherwise
     */
    public boolean isPasswordsMatch() {
        return password != null && password.equals(confirmPassword);
    }

    /**
     * Get full name.
     *
     * @return the full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
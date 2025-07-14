package com.vladte.devhack.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO for User entity.
 * Implements BaseDTO for consistency with other DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User data transfer object")
public class UserDTO implements BaseDTO {

    @Schema(description = "Unique identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    @Schema(description = "User's email address", example = "user@example.com", required = true)
    private String email;

    // Password is intentionally excluded from DTO for security reasons

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Schema(description = "User's name", example = "John Doe", required = true)
    private String name;

    @Schema(description = "User's role", example = "USER", accessMode = Schema.AccessMode.READ_ONLY)
    private String role;

    @Schema(description = "IDs of answers created by this user", accessMode = Schema.AccessMode.READ_ONLY)
    private List<UUID> answerIds = new ArrayList<>();

    @Schema(description = "IDs of notes created by this user", accessMode = Schema.AccessMode.READ_ONLY)
    private List<UUID> noteIds = new ArrayList<>();

    @Schema(description = "IDs of vacancy responses created by this user", accessMode = Schema.AccessMode.READ_ONLY)
    private List<UUID> vacancyResponseIds = new ArrayList<>();

    @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}

package com.vladte.devhack.common.dto;

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
public class UserDTO implements BaseDTO {
    private UUID id;
    private String email;
    // Password is intentionally excluded from DTO for security reasons
    private String name;
    private String role;
    private List<UUID> answerIds = new ArrayList<>();
    private List<UUID> noteIds = new ArrayList<>();
    private List<UUID> vacancyResponseIds = new ArrayList<>();
    private LocalDateTime createdAt;
}
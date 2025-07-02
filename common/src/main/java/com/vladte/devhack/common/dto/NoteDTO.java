package com.vladte.devhack.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Note entity.
 * Implements BaseDTO for consistency with other DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteDTO implements BaseDTO {
    private UUID id;
    private UUID userId;
    private String userName;
    private UUID questionId;
    private String questionText;
    private String noteText;
    private LocalDateTime updatedAt;
}

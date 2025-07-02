package com.vladte.devhack.common.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Answer entity.
 * Implements BaseDTO for consistency with other DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AnswerDTO implements BaseDTO {
    private UUID id;
    private String text;
    private Integer confidenceLevel;
    private Double aiScore;
    private String aiFeedback;
    private Boolean isCorrect;
    private LocalDateTime updatedAt;
    private UUID userId;
    private String userName;
    private UUID questionId;
    private String questionText;
    private LocalDateTime createdAt;
}

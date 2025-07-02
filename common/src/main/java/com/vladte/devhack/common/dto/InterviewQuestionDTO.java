package com.vladte.devhack.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

/**
 * DTO for InterviewQuestion entity.
 * Implements BaseDTO for consistency with other DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewQuestionDTO implements BaseDTO {
    private UUID id;
    private String questionText;
    private String difficulty;
    private String source;
    private UUID userId;
    private String userName;
    private Set<UUID> tagIds = new HashSet<>();
    private Set<String> tagNames = new HashSet<>();
    private List<UUID> answerIds = new ArrayList<>();
    private List<UUID> noteIds = new ArrayList<>();
    private LocalDateTime createdAt;
}
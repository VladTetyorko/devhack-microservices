package com.vladte.devhack.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for Tag entity.
 * Implements BaseDTO for consistency with other DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO implements BaseDTO {
    private UUID id;
    private String name;
    private Set<UUID> questionIds = new HashSet<>();
    private int answeredQuestions;
    private double progressPercentage;
    private LocalDateTime createdAt;

    /**
     * Generate a URL-friendly slug from the tag name.
     * Converts to lowercase and replaces spaces with hyphens.
     *
     * @return the slug
     */
    public String getSlug() {
        if (name == null) {
            return "";
        }
        return name.toLowerCase().replace(' ', '-');
    }
}
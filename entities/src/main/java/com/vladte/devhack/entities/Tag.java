package com.vladte.devhack.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "name", callSuper = false)
public class Tag extends BasicEntity {

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<InterviewQuestion> questions = new HashSet<>();

    @Transient
    private int answeredQuestions = 0;

    @Transient
    private double progressPercentage = 0.0;

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

    /**
     * Calculate the progress percentage for this tag.
     * Progress is defined as the number of answered questions divided by the total number of questions.
     *
     * @return the progress percentage (0-100)
     */
    public double calculateProgressPercentage() {
        int totalQuestions = questions.size();
        if (totalQuestions == 0) {
            return 0.0;
        }
        return (double) answeredQuestions / totalQuestions * 100.0;
    }

    /**
     * Update the progress metrics for this tag.
     * This method should be called after the answeredQuestions count is set.
     */
    public void updateProgress() {
        this.progressPercentage = calculateProgressPercentage();
    }
}

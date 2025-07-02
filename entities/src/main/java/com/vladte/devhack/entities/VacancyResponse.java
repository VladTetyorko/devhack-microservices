package com.vladte.devhack.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a response to a job vacancy.
 */
@Entity
@Table(name = "vacancy_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacancyResponse extends BasicEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "position", nullable = false)
    private String position;

    @Column(name = "technologies", nullable = false)
    private String technologies;

    @Column(name = "pros")
    private String pros;

    @Column(name = "cons")
    private String cons;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "salary")
    private String salary;

    @Column(name = "location")
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_stage", nullable = false)
    private InterviewStage interviewStage = InterviewStage.APPLIED;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "vacancy_response_tags",
            joinColumns = @JoinColumn(name = "vacancy_response_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}
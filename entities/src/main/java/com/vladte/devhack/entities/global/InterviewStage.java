package com.vladte.devhack.entities.global;

import com.vladte.devhack.entities.BasicEntity;
import com.vladte.devhack.entities.personalized.VacancyResponse;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "interview_stage",
        uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewStage extends BasicEntity {

    /**
     * e.g. APPLIED, SCREENING, OFFER, etc.
     */
    @Column(nullable = false, length = 50)
    private String code;

    /**
     * Human label shown in UI
     */
    @Column(nullable = false, length = 100)
    private String label;

    /**
     * Sort order on Kanban board
     */
    @Column(name = "order_index")
    private Integer orderIndex;

    /**
     * e.g. primary, warning
     */
    @Column(name = "color_class", length = 30)
    private String colorClass;

    /**
     * FontAwesome icon class
     */
    @Column(name = "icon_class", length = 50)
    private String iconClass;

    @Column
    private Boolean active;

    @Column(name = "final_stage")
    private Boolean finalStage;

    @Column(name = "internal_only")
    private Boolean internalOnly;

    /**
     * FK to category table
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private InterviewStageCategory category;

    @OneToMany(mappedBy = "interviewStage", cascade = CascadeType.ALL, orphanRemoval = true)
    List<VacancyResponse> responses;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }
}

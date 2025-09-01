package com.vladte.devhack.domain.entities.global;

import com.vladte.devhack.domain.entities.BasicEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@Table(name = "interview_stage_category",
        uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewStageCategory extends BasicEntity {

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(name = "order_index")
    private Integer orderIndex;
}

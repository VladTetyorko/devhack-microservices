package com.vladte.devhack.entities.global;

import com.vladte.devhack.entities.BasicEntity;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "interview_stage_category",
        uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Data
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

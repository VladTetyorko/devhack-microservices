package com.vladte.devhack.entities.personalized;

import com.vladte.devhack.entities.UserOwnedBasicEntity;
import com.vladte.devhack.entities.global.InterviewQuestion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Answer extends UserOwnedBasicEntity {

    @Column(name = "answer_text", nullable = false)
    private String text;

    @Column(name = "confidence_level")
    private Integer confidenceLevel;

    @Column(name = "ai_score")
    private Double aiScore;

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "is_cheating")
    private Boolean isCheating;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private InterviewQuestion question;
}

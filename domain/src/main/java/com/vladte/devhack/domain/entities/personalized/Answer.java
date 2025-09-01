package com.vladte.devhack.domain.entities.personalized;

import com.vladte.devhack.domain.entities.UserOwnedBasicEntity;
import com.vladte.devhack.domain.entities.global.InterviewQuestion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private InterviewQuestion question;
}

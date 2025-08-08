package com.vladte.devhack.entities.personalized;

import com.vladte.devhack.entities.UserOwnedBasicEntity;
import com.vladte.devhack.entities.global.InterviewQuestion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Note extends UserOwnedBasicEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private InterviewQuestion question;

    @Column(name = "note_text", nullable = false)
    private String noteText;
}

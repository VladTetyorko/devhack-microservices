package com.vladte.devhack.entities.personalized;

import com.vladte.devhack.entities.UserOwnedBasicEntity;
import com.vladte.devhack.entities.global.InterviewQuestion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Note extends UserOwnedBasicEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private InterviewQuestion question;

    @Column(name = "note_text", nullable = false)
    private String noteText;
}

package com.vladte.devhack.entities.global;

import com.vladte.devhack.entities.UserOwnedBasicEntity;
import com.vladte.devhack.entities.personalized.Answer;
import com.vladte.devhack.entities.personalized.Note;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "interview_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InterviewQuestion extends UserOwnedBasicEntity {

    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(nullable = false)
    private String difficulty;

    @Column
    private String source;

    @ManyToMany
    @JoinTable(
            name = "question_tags",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Answer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Note> notes = new ArrayList<>();
}

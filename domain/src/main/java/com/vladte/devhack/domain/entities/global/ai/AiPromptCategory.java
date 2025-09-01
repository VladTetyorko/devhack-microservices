package com.vladte.devhack.domain.entities.global.ai;

import com.vladte.devhack.domain.entities.BasicEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_prompts_categories")
public class AiPromptCategory extends BasicEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column
    private String description;

    @Column(nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiPrompt> prompts;
}

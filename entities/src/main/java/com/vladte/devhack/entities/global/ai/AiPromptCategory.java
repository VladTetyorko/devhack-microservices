package com.vladte.devhack.entities.global.ai;

import com.vladte.devhack.entities.BasicEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
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

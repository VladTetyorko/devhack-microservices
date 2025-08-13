package com.vladte.devhack.entities.global.ai;

import com.vladte.devhack.entities.BasicEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_prompts")
public class AiPrompt extends BasicEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(length = 10)
    private String language;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "amount_of_arguments")
    private Integer amountOfArguments;

    @Column(name = "args_description", columnDefinition = "TEXT")
    private String argsDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private AiPromptCategory category;

    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.active == null) this.active = true;
        if (this.language == null) this.language = "en";
    }

}

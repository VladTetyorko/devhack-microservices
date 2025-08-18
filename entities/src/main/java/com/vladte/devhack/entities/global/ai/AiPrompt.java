package com.vladte.devhack.entities.global.ai;

import com.vladte.devhack.entities.BasicEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import io.hypersistence.utils.hibernate.type.json.JsonType;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_prompts")
public class AiPrompt extends BasicEntity {

    @Column(name = "key", nullable = false, unique = true, length = 100)
    private String key;

    @Column
    private String description;

    @Column(name = "system_template", columnDefinition = "TEXT")
    private String systemTemplate;

    @Column(name = "user_template", nullable = false, columnDefinition = "TEXT")
    private String userTemplate;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "args_schema", columnDefinition = "jsonb", nullable = false)
    @Type(JsonType.class)
    private Map<String, Object> argsSchema;

    @Column(name = "defaults", columnDefinition = "jsonb", nullable = false)
    @Type(JsonType.class)
    private Map<String, Object> defaults;

    @Column(name = "model", length = 50, nullable = false)
    private String model;

    @Column(name = "parameters", columnDefinition = "jsonb", nullable = false)
    @Type(JsonType.class)
    private Map<String, Object> parameters;

    @Column(name = "response_contract", columnDefinition = "jsonb")
    @Type(JsonType.class)
    private Map<String, Object> responseContract;

    @Column(name = "version", nullable = false)
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private AiPromptCategory category;

    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.enabled == null) this.enabled = true;
        if (this.model == null) this.model = "gpt-3.5-turbo";
        if (this.version == null) this.version = 1;
        if (this.argsSchema == null) this.argsSchema = Map.of();
        if (this.defaults == null) this.defaults = Map.of();
        if (this.parameters == null) this.parameters = Map.of();
    }
}

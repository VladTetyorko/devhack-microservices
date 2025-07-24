package com.vladte.devhack.infra.model.arguments.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladte.devhack.infra.model.arguments.KafkaPayloadArguments;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Builder
@EqualsAndHashCode(callSuper = true)
public class AnswerCheckResponseArguments extends KafkaPayloadArguments {

    private boolean hasCheating;

    private double score;

    private String feedback;

    @Override
    @JsonIgnore
    public List<String> getAsList() {
        return List.of(String.valueOf(hasCheating), String.valueOf(score), feedback);
    }

    @JsonIgnore
    @Override
    public boolean necessaryArgumentsAreEmpty() {
        return false;
    }
}

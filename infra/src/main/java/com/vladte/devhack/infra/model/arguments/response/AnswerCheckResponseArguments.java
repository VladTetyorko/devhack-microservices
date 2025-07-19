package com.vladte.devhack.infra.model.arguments.response;

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
    public List<String> getAsList() {
        return List.of(String.valueOf(hasCheating), String.valueOf(score), feedback);
    }

    @Override
    public boolean necessaryArgumentsAreEmpty() {
        return false;
    }
}

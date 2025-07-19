package com.vladte.devhack.infra.model.arguments.request;

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
public class AnswerCheckRequestArguments extends KafkaPayloadArguments {

    private String question;

    private String answer;

    private boolean checkCheating;

    @Override
    @JsonIgnore
    public List<String> getAsList() {
        return List.of(question, answer, String.valueOf(checkCheating));
    }

    @Override
    @JsonIgnore
    public boolean necessaryArgumentsAreEmpty() {
        return question.isEmpty() && answer.isEmpty();
    }
}

package com.vladte.devhack.infra.model.arguments.response;

import com.vladte.devhack.infra.model.arguments.KafkaPayloadArguments;
import lombok.*;

import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Builder
public class QuestionGenerateResponseArguments extends KafkaPayloadArguments {

    String[] questions;

    @Override
    public List<String> getAsList() {
        return Arrays.asList(questions.clone());
    }

    @Override
    public boolean necessaryArgumentsAreEmpty() {
        return false;
    }
}

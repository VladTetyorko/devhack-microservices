package com.vladte.devhack.infra.model.arguments.response;

import com.vladte.devhack.infra.model.arguments.KafkaPayloadArguments;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@ToString(callSuper = true)
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VacancyParseResultArguments extends KafkaPayloadArguments {

    private String vacancyJson;

    @Override
    public List<String> getAsList() {
        return List.of(vacancyJson);
    }

    @Override
    public boolean necessaryArgumentsAreEmpty() {
        return false;
    }
}

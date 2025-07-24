package com.vladte.devhack.infra.model.arguments.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladte.devhack.infra.model.arguments.KafkaPayloadArguments;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VacancyParseResultArguments extends KafkaPayloadArguments {

    private String vacancyJson;

    @JsonIgnore
    @Override
    public List<String> getAsList() {
        return List.of(vacancyJson);
    }

    @JsonIgnore
    @Override
    public boolean necessaryArgumentsAreEmpty() {
        return false;
    }
}

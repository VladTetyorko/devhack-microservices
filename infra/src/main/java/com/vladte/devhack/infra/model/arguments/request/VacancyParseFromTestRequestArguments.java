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
public class VacancyParseFromTestRequestArguments extends KafkaPayloadArguments {

    private String fieldsToParse;

    private String text;

    @Override
    @JsonIgnore
    public List<String> getAsList() {
        return List.of(fieldsToParse, text);
    }

    @Override
    @JsonIgnore
    public boolean necessaryArgumentsAreEmpty() {
        return fieldsToParse.isEmpty() && text.isEmpty();
    }
}

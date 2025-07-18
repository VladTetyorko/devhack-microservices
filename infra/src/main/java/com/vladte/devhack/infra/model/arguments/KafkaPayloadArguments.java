package com.vladte.devhack.infra.model.arguments;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@NoArgsConstructor
public abstract class KafkaPayloadArguments {

    @JsonIgnore
    public abstract List<String> getAsList();

    @JsonIgnore
    public abstract boolean necessaryArgumentsAreEmpty();
}

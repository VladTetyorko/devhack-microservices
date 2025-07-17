package com.vladte.devhack.infra.model.payload;

import com.vladte.devhack.infra.model.arguments.KafkaPayloadArguments;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
public abstract class AiRequestPayload<T extends KafkaPayloadArguments> {
    private String prompt;

    private T arguments;

    private String language;
}
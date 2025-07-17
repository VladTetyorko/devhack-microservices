package com.vladte.devhack.infra.model.payload;

import com.vladte.devhack.infra.model.arguments.KafkaPayloadArguments;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString
public abstract class AiResponsePayload<T extends KafkaPayloadArguments> {

    private T arguments;

    private boolean hasErrors;

    private String errorMessage;

}

package com.vladte.devhack.infra.model.payload.response;

import com.vladte.devhack.infra.model.arguments.response.VacancyParseResultArguments;
import com.vladte.devhack.infra.model.payload.ResponsePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class VacancyParseResponsePayload extends ResponsePayload<VacancyParseResultArguments> {

    public static VacancyParseResponsePayload error(String message) {
        return VacancyParseResponsePayload.builder()
                .hasErrors(true)
                .errorMessage(message)
                .build();
    }

    public static VacancyParseResponsePayload fromJson(String json) {
        return VacancyParseResponsePayload.builder()
                .arguments(VacancyParseResultArguments.builder()
                        .vacancyJson(json)
                        .build())
                .build();
    }

}

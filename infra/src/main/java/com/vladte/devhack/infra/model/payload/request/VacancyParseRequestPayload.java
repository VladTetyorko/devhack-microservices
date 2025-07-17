package com.vladte.devhack.infra.model.payload.request;


import com.vladte.devhack.infra.model.arguments.request.VacancyParseFromTestRequestArguments;
import com.vladte.devhack.infra.model.payload.AiRequestPayload;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
public class VacancyParseRequestPayload extends AiRequestPayload<VacancyParseFromTestRequestArguments> {

}

package com.vladte.devhack.infra.model.payload.request;

import com.vladte.devhack.infra.model.arguments.request.QuestionGenerateRequestArguments;
import com.vladte.devhack.infra.model.payload.AiRequestPayload;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
public class QuestionGenerateRequestPayload extends AiRequestPayload<QuestionGenerateRequestArguments> {
}

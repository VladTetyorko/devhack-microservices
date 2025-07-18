package com.vladte.devhack.infra.model.payload.response;

import com.vladte.devhack.infra.model.arguments.response.QuestionGenerateResponseArguments;
import com.vladte.devhack.infra.model.payload.ResponsePayload;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class QuestionGenerateResponsePayload extends ResponsePayload<QuestionGenerateResponseArguments> {


    public static QuestionGenerateResponsePayload error(String invalidPayloadFormat) {
        return QuestionGenerateResponsePayload.builder()
                .hasErrors(true)
                .errorMessage(invalidPayloadFormat)
                .build();
    }

    public static QuestionGenerateResponsePayload fromGeneratedText(String result) {
        String[] questions = result.split("\n");
        return QuestionGenerateResponsePayload.builder()
                .arguments(
                        QuestionGenerateResponseArguments.builder()
                                .questions(questions)
                                .build()
                ).build();
    }
}

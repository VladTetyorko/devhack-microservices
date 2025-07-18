package com.vladte.devhack.infra.model.payload.response;

import com.vladte.devhack.infra.model.arguments.response.AnswerCheckResponseArguments;
import com.vladte.devhack.infra.model.payload.ResponsePayload;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class AnswerCheckResponsePayload extends ResponsePayload<AnswerCheckResponseArguments> {

    public static AnswerCheckResponsePayload fromCheatingResult(Boolean isCheating) {
        return builder()
                .arguments(
                        AnswerCheckResponseArguments.builder().hasCheating(isCheating)
                                .build())
                .build();
    }

    public static AnswerCheckResponsePayload fromScoreAndFeedback(Map<String, Object> result) {
        return AnswerCheckResponsePayload.builder().arguments(
                AnswerCheckResponseArguments.builder()
                        .score((Double) result.getOrDefault("score", 0.0))
                        .feedback((String) result.getOrDefault("feedback", ""))
                        .build()
        ).build();
    }

    public static AnswerCheckResponsePayload error(String errorMessage) {
        return AnswerCheckResponsePayload.builder()
                .hasErrors(true)
                .errorMessage(errorMessage)
                .build();
    }

}

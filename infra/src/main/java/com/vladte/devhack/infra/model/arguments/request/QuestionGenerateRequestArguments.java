package com.vladte.devhack.infra.model.arguments.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladte.devhack.infra.model.arguments.KafkaPayloadArguments;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Builder
public class QuestionGenerateRequestArguments extends KafkaPayloadArguments {
    private String tag;
    private Integer count;
    private String difficulty;

    @Override
    @JsonIgnore
    public List<String> getAsList() {
        return List.of(tag, count.toString(), difficulty);
    }

    @Override
    @JsonIgnore
    public boolean necessaryArgumentsAreEmpty() {
        return tag.isEmpty() && !Objects.nonNull(count) && difficulty.isEmpty();
    }
}

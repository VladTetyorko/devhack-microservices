package com.vladte.devhack.infra.model.payload;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Base payload for any AI request.
 * Holds prompt identification, raw/template prompt, generic arguments,
 * localization, model selection and provider parameters, and the expected
 * response contract (schema) with its version.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class RequestPayload {
    private String promptId;
    private String promptKey;

    private String prompt;

    private JsonNode arguments;

    private String language;

    private String model;
    private Map<String, Object> parameters;

    private JsonNode responseContract;
    private Integer version;
}

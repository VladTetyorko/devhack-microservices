package com.vladte.devhack.infra.model.payload.request;

import com.vladte.devhack.infra.model.payload.RequestPayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Rendered/chat request payload.
 * Adds rendered input and (optionally) chat-style messages.
 * All generic fields (promptId/key, model, parameters, responseContract, etc.)
 * live in the parent to avoid duplication.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AiRenderedRequestPayload extends RequestPayload {

    private String input;

    private List<Message> messages;
}

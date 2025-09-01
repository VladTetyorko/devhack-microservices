package com.vladte.devhack.common.engine.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.samskivert.mustache.Mustache;
import com.vladte.devhack.domain.entities.global.ai.AiPrompt;
import com.vladte.devhack.infra.model.payload.request.AiRenderedRequestPayload;
import com.vladte.devhack.infra.model.payload.request.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public final class PromptEngine {

    private static final Logger log = LoggerFactory.getLogger(PromptEngine.class);

    private final ObjectMapper objectMapper;
    private final JsonSchemaFactory schemaFactory;
    private final Mustache.Compiler mustache;
    private final ArgsBinder argsBinder;

    public PromptEngine(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        this.mustache = Mustache.compiler().escapeHTML(false);
        this.argsBinder = new ArgsBinder(this.objectMapper);
        log.debug("PromptEngine initialized (schema=V202012, mustache.escapeHTML=false)");
    }

    public AiRenderedRequestPayload render(AiPrompt prompt, Map<String, Object> rawArgs) {
        Objects.requireNonNull(prompt, "prompt");
        long started = System.nanoTime();

        log.info("Rendering prompt key='{}', model='{}', version='{}'", prompt.getKey(), prompt.getModel(), prompt.getVersion());
        log.debug("Incoming args keys: {}", rawArgs != null ? rawArgs.keySet() : Collections.emptySet());
        log.debug("Has systemTemplate: {}, has userTemplate: {}", notBlank(prompt.getSystemTemplate()), notBlank(prompt.getUserTemplate()));

        Map<String, Object> args = mergeArgs(prompt, rawArgs);
        validateArgs(prompt.getArgsSchema(), args);

        String system = renderTemplate(prompt.getSystemTemplate(), args);
        String user = renderTemplate(prompt.getUserTemplate(), args);
        log.debug("Rendered system length: {}, user length: {}", lengthOf(system), lengthOf(user));

        Map<String, Object> params = normalizeParameters(prompt);
        List<Message> messages = buildMessages(system, user);
        log.debug("Assembled {} message(s): roles={}", messages.size(), messages.stream().map(Message::getRole).toList());
        log.debug("Parameters keys: {}", params.keySet());

        AiRenderedRequestPayload payload = buildRenderedPrompt(prompt, params, messages, user);

        long elapsedMs = (System.nanoTime() - started) / 1_000_000;
        log.info("Prompt rendered: key='{}', messages={}, elapsed={}ms", prompt.getKey(), messages.size(), elapsedMs);
        return payload;
    }

    public AiRenderedRequestPayload render(AiPrompt prompt, Object... sources) {
        Objects.requireNonNull(prompt, "prompt");
        log.debug("Binding args for prompt key='{}' from {} source(s)", prompt.getKey(), sources != null ? sources.length : 0);
        Map<String, Object> bound = argsBinder.bind(prompt.getArgsSchema(), prompt.getDefaults(), sources);
        log.debug("Bound args keys: {}", bound.keySet());
        return render(prompt, bound);
    }

    private Map<String, Object> mergeArgs(AiPrompt prompt, Map<String, Object> rawArgs) {
        Map<String, Object> args = new HashMap<>();
        if (prompt.getDefaults() != null && !prompt.getDefaults().isEmpty()) {
            args.putAll(prompt.getDefaults());
            log.debug("Applied defaults keys: {}", prompt.getDefaults().keySet());
        }
        if (rawArgs != null && !rawArgs.isEmpty()) {
            args.putAll(rawArgs);
            log.debug("Applied runtime args override keys: {}", rawArgs.keySet());
        }
        return args;
    }

    private String renderTemplate(String template, Map<String, Object> args) {
        if (!notBlank(template)) {
            return null;
        }
        return mustache.compile(template).execute(args);
    }

    private Map<String, Object> normalizeParameters(AiPrompt prompt) {
        Map<String, Object> params = new HashMap<>();
        if (prompt.getParameters() != null && !prompt.getParameters().isEmpty()) {
            params.putAll(prompt.getParameters());
        }
        return params;
    }

    private List<Message> buildMessages(String system, String user) {
        List<Message> messages = new ArrayList<>(2);
        if (notBlank(system)) messages.add(new Message("system", system));
        if (notBlank(user)) messages.add(new Message("user", user));
        return messages;
    }

    private AiRenderedRequestPayload buildRenderedPrompt(AiPrompt prompt,
                                                         Map<String, Object> params,
                                                         List<Message> messages,
                                                         String user) {
        return AiRenderedRequestPayload.builder()
                .promptId(prompt.getId() != null ? prompt.getId().toString() : null)
                .promptKey(prompt.getKey())
                .model(prompt.getModel())
                .parameters(params)
                .messages(messages)
                .input(user)
                .responseContract(objectMapper.valueToTree(prompt.getResponseContract()))
                .version(prompt.getVersion())
                .build();
    }

    private void validateArgs(Object argsSchema, Map<String, Object> args) {
        JsonNode argsSchemaNode = objectMapper.valueToTree(argsSchema);
        JsonSchema argSchema = schemaFactory.getSchema(argsSchemaNode);
        Set<ValidationMessage> errors = argSchema.validate(objectMapper.valueToTree(args));
        if (!errors.isEmpty()) {
            log.warn("Args validation failed with {} error(s): {}", errors.size(), errors);
            throw new IllegalArgumentException("Args validation failed: " + errors);
        }
        log.debug("Args validation passed for keys: {}", args.keySet());
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static int lengthOf(String s) {
        return s == null ? 0 : s.length();
    }
}

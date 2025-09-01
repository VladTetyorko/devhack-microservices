package com.vladte.devhack.common.engine.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.domain.entities.global.ai.AiPrompt;
import com.vladte.devhack.infra.model.payload.request.AiRenderedRequestPayload;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public class PromptEngineTest {

    private ObjectMapper om;
    private PromptEngine engine;

    @BeforeMethod
    public void setUp() {
        this.om = new ObjectMapper();
        this.engine = new PromptEngine(om);
    }

    private Map<String, Object> simpleArgsSchema() {
        Map<String, Object> props = new HashMap<>();
        props.put("tag", Map.of("type", "string"));
        props.put("count", Map.of("type", "integer", "default", 3));
        props.put("difficulty", Map.of("type", "string", "default", "MEDIUM"));
        return Map.of("type", "object", "properties", props, "required", List.of("tag"));
    }

    private AiPrompt buildPrompt() {
        return AiPrompt.builder()
                .key("ai.question.generate")
                .systemTemplate("You are a system for {{tag}}")
                .userTemplate("Generate {{count}} {{difficulty}} questions about {{tag}}.")
                .argsSchema(simpleArgsSchema())
                .defaults(Map.of("difficulty", "EASY"))
                .model("gpt-test")
                .parameters(Map.of("temperature", 0))
                .responseContract(Map.of("type", "object", "properties", Map.of("questions", Map.of("type", "array"))))
                .version(1)
                .enabled(true)
                .build();
    }

    @Test
    public void testRenderSuccessWithDefaultsMerge() {
        AiPrompt prompt = buildPrompt();
        Map<String, Object> args = Map.of("tag", "Spring", "count", 5);

        AiRenderedRequestPayload rendered = engine.render(prompt, args);

        assertNotNull(rendered);
        assertEquals(rendered.getModel(), "gpt-test");
        assertEquals(rendered.getParameters().get("temperature"), 0);
        assertEquals(rendered.getVersion(), Integer.valueOf(1));
        assertNotNull(rendered.getResponseContract());

        // messages: system + user present
        assertNotNull(rendered.getMessages());
        assertFalse(rendered.getMessages().isEmpty());
        String userContent = rendered.getInput();
        assertTrue(userContent.contains("5"));
        assertTrue(userContent.contains("Spring"));
        // default difficulty (EASY) should be used because not provided in args
        assertTrue(userContent.contains("EASY"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidationFailure() {
        AiPrompt prompt = buildPrompt();
        // missing required "tag" -> schema has type string for tag but no default; thus invalid
        Map<String, Object> badArgs = Map.of("count", 2);
        engine.render(prompt, badArgs);
    }

    @Test
    public void testRenderWithArgsBinderOverload() {
        AiPrompt prompt = buildPrompt();
        Map<String, Object> nested = Map.of(
                "payload", Map.of(
                        "topic", Map.of("name", "Java"),
                        "n", 7,
                        "level", "HARD"
                )
        );

        // extend schema with x-path to test binder path resolution
        Map<String, Object> props = new HashMap<>((Map<String, Object>) prompt.getArgsSchema().get("properties"));
        Map<String, Object> tagSchema = new HashMap<>((Map<String, Object>) props.get("tag"));
        tagSchema.put("x-path", "/payload/topic/name");
        props.put("tag", tagSchema);
        props.put("count", Map.of("type", "integer", "x-path", "/payload/n"));
        props.put("difficulty", Map.of("type", "string", "x-path", "/payload/level"));
        Map<String, Object> newSchema = Map.of("type", "object", "properties", props, "required", List.of("tag"));
        prompt.setArgsSchema(newSchema);

        // sanity: binder extracts values
        Map<String, Object> bound = new ArgsBinder(om).bind(newSchema, prompt.getDefaults(), nested);
        assertEquals(bound.get("tag"), "Java");
        assertEquals(bound.get("count"), 7L);
        assertEquals(bound.get("difficulty"), "HARD");

        AiRenderedRequestPayload rendered = engine.render(prompt, (Object) nested);
        assertTrue(rendered.getInput().contains("7"));
        assertTrue(rendered.getInput().contains("Java"));
        assertTrue(rendered.getInput().contains("HARD"));
    }
}

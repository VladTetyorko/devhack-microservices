package com.vladte.devhack.common.service.generations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.common.service.generations.impl.PromptRenderServiceImpl;
import com.vladte.devhack.entities.global.ai.AiPrompt;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PromptRenderServiceImplTest {

    @Test
    void renderAndValidate_withValidParams_shouldSucceed() {
        ObjectMapper objectMapper = new ObjectMapper();
        PromptRenderServiceImpl service = new PromptRenderServiceImpl(objectMapper);

        AiPrompt prompt = AiPrompt.builder()
                .key("test.prompt")
                .systemTemplate("System: You are helpful.")
                .userTemplate("Hello {{name}}, generate {{count}} items.")
                .argsSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "name", Map.of("type", "string"),
                                "count", Map.of("type", "integer", "minimum", 1, "maximum", 10)
                        ),
                        "required", new String[]{"name"}
                ))
                .defaults(Map.of("count", 3))
                .model("gpt-3.5-turbo")
                .parameters(Map.of("temperature", 0.2))
                .enabled(true)
                .version(1)
                .build();

        Map<String, Object> params = Map.of("name", "DevHack", "count", 2);

        // validate
        assertDoesNotThrow(() -> service.validateParameters(prompt, params));

        // user template
        String user = service.renderUserTemplate(prompt, params);
        assertTrue(user.contains("DevHack"));
        assertTrue(user.contains("2"));

        // system template
        String system = service.renderSystemTemplate(prompt, params);
        assertNotNull(system);
        assertTrue(system.startsWith("System:"));

        // full prompt
        String full = service.renderPrompt(prompt, params);
        assertTrue(full.contains("You are helpful"));
        assertTrue(full.contains("Hello DevHack"));
    }

    @Test
    void validate_withInvalidParams_shouldThrow() {
        ObjectMapper objectMapper = new ObjectMapper();
        PromptRenderServiceImpl service = new PromptRenderServiceImpl(objectMapper);

        AiPrompt prompt = AiPrompt.builder()
                .key("test.prompt")
                .userTemplate("Hi {{name}}")
                .argsSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "name", Map.of("type", "string")
                        ),
                        "required", new String[]{"name"}
                ))
                .defaults(Map.of())
                .model("gpt-3.5-turbo")
                .parameters(Map.of())
                .enabled(true)
                .version(1)
                .build();

        Map<String, Object> badParams = Map.of("name", 123); // wrong type

        assertThrows(IllegalArgumentException.class, () -> service.validateParameters(prompt, badParams));
    }
}

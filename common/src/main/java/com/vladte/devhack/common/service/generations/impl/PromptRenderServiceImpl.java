package com.vladte.devhack.common.service.generations.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.vladte.devhack.common.service.generations.PromptRenderService;
import com.vladte.devhack.entities.global.ai.AiPrompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of PromptRenderService using Mustache.java for template rendering
 * and networknt JSON Schema for parameter validation.
 * <p>
 * Features:
 * - Template caching for performance
 * - Comprehensive parameter validation
 * - Default value merging
 * - Error handling with detailed messages
 * - Thread-safe operations
 */
@Service
public class PromptRenderServiceImpl implements PromptRenderService {

    private static final Logger log = LoggerFactory.getLogger(PromptRenderServiceImpl.class);

    private final MustacheFactory mustacheFactory;
    private final ObjectMapper objectMapper;
    private final JsonSchemaFactory schemaFactory;

    // Cache for compiled templates to improve performance
    private final Map<String, Mustache> templateCache = new ConcurrentHashMap<>();

    // Cache for compiled schemas to improve performance
    private final Map<String, JsonSchema> schemaCache = new ConcurrentHashMap<>();

    public PromptRenderServiceImpl(ObjectMapper objectMapper) {
        this.mustacheFactory = new DefaultMustacheFactory();
        this.objectMapper = objectMapper;
        this.schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    }

    @Override
    public String renderPrompt(AiPrompt prompt, Map<String, Object> parameters) {
        log.debug("Rendering prompt with key: {}", prompt.getKey());

        // Validate and merge parameters
        Map<String, Object> mergedParams = mergeWithDefaults(prompt, parameters);
        validateParameters(prompt, mergedParams);

        // Render user template (required)
        String userTemplate = renderUserTemplate(prompt, mergedParams);

        // Render system template if present
        String systemTemplate = renderSystemTemplate(prompt, mergedParams);

        // Combine templates if both are present
        if (systemTemplate != null && !systemTemplate.trim().isEmpty()) {
            return systemTemplate + "\n\n" + userTemplate;
        }

        return userTemplate;
    }

    @Override
    public String renderSystemTemplate(AiPrompt prompt, Map<String, Object> parameters) {
        if (prompt.getSystemTemplate() == null || prompt.getSystemTemplate().trim().isEmpty()) {
            return null;
        }
        // Ensure parameters are merged with defaults when called directly
        Map<String, Object> mergedParams = mergeWithDefaults(prompt, parameters);

        log.debug("Rendering system template for prompt: {}", prompt.getKey());
        return renderTemplate(prompt.getSystemTemplate(), mergedParams, computeCacheKey("system", prompt, prompt.getSystemTemplate()));
    }

    @Override
    public String renderUserTemplate(AiPrompt prompt, Map<String, Object> parameters) {
        if (prompt.getUserTemplate() == null || prompt.getUserTemplate().trim().isEmpty()) {
            throw new IllegalArgumentException("User template is required for prompt: " + prompt.getKey());
        }
        // Ensure parameters are merged with defaults and validated when called directly
        Map<String, Object> mergedParams = mergeWithDefaults(prompt, parameters);
        validateParameters(prompt, mergedParams);

        log.debug("Rendering user template for prompt: {}", prompt.getKey());
        return renderTemplate(prompt.getUserTemplate(), mergedParams, computeCacheKey("user", prompt, prompt.getUserTemplate()));
    }

    @Override
    public void validateParameters(AiPrompt prompt, Map<String, Object> parameters) {
        if (prompt.getArgsSchema() == null || prompt.getArgsSchema().isEmpty()) {
            log.debug("No schema defined for prompt: {}, skipping validation", prompt.getKey());
            return;
        }

        try {
            // Get or create cached schema
            String schemaKey = prompt.getKey() + "_v" + prompt.getVersion();
            JsonSchema schema = schemaCache.computeIfAbsent(schemaKey, k -> {
                try {
                    JsonNode schemaNode = objectMapper.valueToTree(prompt.getArgsSchema());
                    return schemaFactory.getSchema(schemaNode);
                } catch (Exception e) {
                    log.error("Failed to create schema for prompt: {}", prompt.getKey(), e);
                    throw new RuntimeException("Invalid schema for prompt: " + prompt.getKey(), e);
                }
            });

            // Convert parameters to JsonNode for validation
            JsonNode parametersNode = objectMapper.valueToTree(parameters);

            // Validate parameters against schema
            Set<ValidationMessage> validationMessages = schema.validate(parametersNode);

            if (!validationMessages.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder("Parameter validation failed for prompt: ")
                        .append(prompt.getKey()).append(". Errors: ");

                validationMessages.forEach(msg ->
                        errorMessage.append("[").append(msg.getEvaluationPath()).append(": ").append(msg.getMessage()).append("] "));

                throw new IllegalArgumentException(errorMessage.toString());
            }

            log.debug("Parameter validation successful for prompt: {}", prompt.getKey());

        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            log.error("Error during parameter validation for prompt: {}", prompt.getKey(), e);
            throw new RuntimeException("Parameter validation error for prompt: " + prompt.getKey(), e);
        }
    }

    @Override
    public Map<String, Object> mergeWithDefaults(AiPrompt prompt, Map<String, Object> parameters) {
        Map<String, Object> merged = new HashMap<>();

        // Start with defaults
        if (prompt.getDefaults() != null) {
            merged.putAll(prompt.getDefaults());
        }

        // Override with provided parameters
        if (parameters != null) {
            merged.putAll(parameters);
        }

        log.debug("Merged parameters for prompt: {} - defaults: {}, provided: {}, result: {}",
                prompt.getKey(), prompt.getDefaults(), parameters, merged);

        return merged;
    }

    /**
     * Computes a stable cache key for a template variant combining prompt key, version and template hash.
     */
    private String computeCacheKey(String part, AiPrompt prompt, String template) {
        String version = prompt.getVersion() == null ? "0" : String.valueOf(prompt.getVersion());
        String key = prompt.getKey() == null ? "unknown" : prompt.getKey();
        int hash = template == null ? 0 : template.hashCode();
        return part + "_" + key + "_v" + version + "_" + Integer.toHexString(hash);
    }

    /**
     * Renders a template string with the provided parameters using Mustache.
     *
     * @param template   the template string
     * @param parameters the parameters to render with
     * @param cacheKey   the key for template caching
     * @return the rendered template
     */
    private String renderTemplate(String template, Map<String, Object> parameters, String cacheKey) {
        try {
            // Get or compile template
            Mustache mustache = templateCache.computeIfAbsent(cacheKey, k -> {
                try {
                    return mustacheFactory.compile(new StringReader(template), cacheKey);
                } catch (Exception e) {
                    log.error("Failed to compile template for key: {}", cacheKey, e);
                    throw new RuntimeException("Template compilation failed for: " + cacheKey, e);
                }
            });

            // Render template
            StringWriter writer = new StringWriter();
            mustache.execute(writer, parameters);
            String result = writer.toString();

            log.debug("Template rendered successfully for key: {}", cacheKey);
            return result;

        } catch (Exception e) {
            log.error("Error rendering template for key: {}", cacheKey, e);
            throw new RuntimeException("Template rendering failed for: " + cacheKey, e);
        }
    }

    /**
     * Clears the template and schema caches. Useful for testing or when templates change.
     */
    public void clearCaches() {
        templateCache.clear();
        schemaCache.clear();
        log.info("Template and schema caches cleared");
    }

    /**
     * Gets the current size of the template cache.
     *
     * @return the number of cached templates
     */
    public int getTemplateCacheSize() {
        return templateCache.size();
    }

    /**
     * Gets the current size of the schema cache.
     *
     * @return the number of cached schemas
     */
    public int getSchemaCacheSize() {
        return schemaCache.size();
    }
}
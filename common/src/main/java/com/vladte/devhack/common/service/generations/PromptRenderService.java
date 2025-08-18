package com.vladte.devhack.common.service.generations;

import com.vladte.devhack.entities.global.ai.AiPrompt;

import java.util.Map;

/**
 * Service for rendering AI prompts with parameter validation and template processing.
 * Uses Mustache.java for template rendering and networknt JSON Schema for parameter validation.
 * <p>
 * Follows SOLID principles:
 * - Single Responsibility: Handles only prompt rendering and validation
 * - Open/Closed: Extensible for different template engines
 * - Liskov Substitution: Can be substituted with other implementations
 * - Interface Segregation: Focused interface for prompt rendering
 * - Dependency Inversion: Depends on abstractions, not concretions
 */
public interface PromptRenderService {

    /**
     * Renders a prompt template with the provided parameters.
     * Validates parameters against the prompt's JSON schema before rendering.
     *
     * @param prompt     the AI prompt entity containing templates and schema
     * @param parameters the parameters to render the template with
     * @return the rendered prompt text
     * @throws IllegalArgumentException if parameters don't match the schema
     * @throws RuntimeException         if template rendering fails
     */
    String renderPrompt(AiPrompt prompt, Map<String, Object> parameters);

    /**
     * Renders the system template of a prompt with the provided parameters.
     *
     * @param prompt     the AI prompt entity containing the system template
     * @param parameters the parameters to render the template with
     * @return the rendered system prompt text, or null if no system template
     * @throws IllegalArgumentException if parameters don't match the schema
     * @throws RuntimeException         if template rendering fails
     */
    String renderSystemTemplate(AiPrompt prompt, Map<String, Object> parameters);

    /**
     * Renders the user template of a prompt with the provided parameters.
     *
     * @param prompt     the AI prompt entity containing the user template
     * @param parameters the parameters to render the template with
     * @return the rendered user prompt text
     * @throws IllegalArgumentException if parameters don't match the schema
     * @throws RuntimeException         if template rendering fails
     */
    String renderUserTemplate(AiPrompt prompt, Map<String, Object> parameters);

    /**
     * Validates parameters against the prompt's JSON schema.
     *
     * @param prompt     the AI prompt entity containing the schema
     * @param parameters the parameters to validate
     * @throws IllegalArgumentException if parameters don't match the schema
     */
    void validateParameters(AiPrompt prompt, Map<String, Object> parameters);

    /**
     * Merges provided parameters with default values from the prompt.
     * Default values are used for missing parameters.
     *
     * @param prompt     the AI prompt entity containing default values
     * @param parameters the provided parameters
     * @return merged parameters with defaults applied
     */
    Map<String, Object> mergeWithDefaults(AiPrompt prompt, Map<String, Object> parameters);
}
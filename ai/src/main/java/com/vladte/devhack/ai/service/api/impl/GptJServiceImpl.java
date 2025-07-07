package com.vladte.devhack.ai.service.api.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.ai.service.api.AbstractAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the OpenAiService interface for interacting with the GPT-J API via LocalAI.
 */
@Profile("local")
@Service("gptJService")
public class GptJServiceImpl extends AbstractAiService {
    private static final Logger log = LoggerFactory.getLogger(GptJServiceImpl.class);

    @Autowired
    public GptJServiceImpl(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Value("${gptj.api.key}")
    private String apiKey;

    @Value("${gptj.model}")
    private String model;

    @Value("${gptj.max-tokens}")
    private int maxTokens;

    @Value("${gptj.api.url}")
    private String apiUrl;


    @Override
    protected String getApiKey() {
        return apiKey;
    }

    @Override
    protected String getModel() {
        return model;
    }

    @Override
    protected int getMaxTokens() {
        return maxTokens;
    }

    @Override
    protected String getApiUrl() {
        return apiUrl;
    }

    /**
     * Override to create a request body appropriate for the completions API.
     */
    @Override
    protected HttpEntity<Map<String, Object>> createApiRequest(String prompt) {
        log.debug("Creating API request for completions API with model: {}", getModel());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getApiKey());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", getModel());
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", getMaxTokens());

        return new HttpEntity<>(requestBody, headers);
    }

    /**
     * Override to parse the response from the completions API.
     */
    @Override
    protected String parseApiResponse(Map<String, Object> responseBody) {
        log.debug("Parsing completions API response");

        if (responseBody == null) {
            log.warn("Received null response body");
            return "Failed to generate text: null response";
        }

        log.debug("Response body: {}", responseBody);

        // First try the standard completions API format with choices
        if (responseBody.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");

            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                if (choice.containsKey("text")) {
                    String text = (String) choice.get("text");
                    log.debug("Successfully parsed response with choices, text length: {}", text.length());
                    return text;
                }
            }
        }

        // If we can't find the expected structure, try to make a request again
        // The logs show that sometimes the API returns a response with no content
        // but with fields like "created", "object", "id", "model", "usage"
        if (responseBody.containsKey("model") && responseBody.containsKey("usage")) {
            Map<String, Object> usage = (Map<String, Object>) responseBody.get("usage");
            if (usage != null && usage.containsKey("completion_tokens")) {
                Integer completionTokens = (Integer) usage.get("completion_tokens");
                if (completionTokens != null && completionTokens == 0) {
                    log.warn("Received response with zero completion tokens, retrying...");
                    // Return a message indicating the issue
                    return "The AI model returned an empty response. Please try again with a different prompt.";
                }
            }
        }

        // If we can't find any expected structure, return an error message
        log.warn("Response does not contain expected structure: {}", responseBody);
        return "Failed to generate text: invalid response format";
    }
}

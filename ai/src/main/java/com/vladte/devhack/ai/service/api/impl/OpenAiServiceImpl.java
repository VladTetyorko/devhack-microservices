package com.vladte.devhack.ai.service.api.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.ai.service.api.AbstractAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the OpenAiService interface for interacting with the OpenAI API.
 */
@Service("openApiService")
public class OpenAiServiceImpl extends AbstractAiService {

    @Value("${openai.model}")
    private String model;

    @Value("${openai.max-tokens}")
    private int maxTokens;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    private static final Logger log = LoggerFactory.getLogger(OpenAiServiceImpl.class);

    public OpenAiServiceImpl(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected HttpEntity<Map<String, Object>> createApiRequestBody(String prompt) {
        log.debug("Creating API request for legacy text prompt model: {}", getModel());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getApiKey());

        log.debug("Creating legacy request body (single user message) with prompt length: {}", prompt.length());
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", getModel());
        requestBody.put("messages", List.of(message));
        return new HttpEntity<>(requestBody, headers);
    }

    @Override
    protected String getApiKey() {
        String key = System.getenv("OPENAI_API_KEY");
        if (key == null) {
            throw new IllegalStateException("OPENAI_API_KEY not set in environment");
        }
        return key;
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
        return OPENAI_API_URL;
    }
}
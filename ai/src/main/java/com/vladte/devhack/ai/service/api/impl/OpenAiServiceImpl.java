package com.vladte.devhack.ai.service.api.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.ai.service.api.AbstractAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Implementation of the OpenAiService interface for interacting with the OpenAI API.
 */
@Service("openApiService")
public class OpenAiServiceImpl extends AbstractAiService {

    private static final String MODEL_NAME = "gpt-4";

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.max-tokens}")
    private int maxTokens;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    public OpenAiServiceImpl(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected String getModelName() {
        return MODEL_NAME;
    }

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
        return OPENAI_API_URL;
    }
}
package com.vladte.devhack.ai.service.api;

import com.vladte.devhack.ai.service.api.impl.GptJServiceImpl;
import com.vladte.devhack.ai.service.api.impl.OpenAiServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiServiceFactory {

    @Value("${ai.service.provider}")
    private String provider;

    @Bean
    public AbstractAiService aiService(
            OpenAiServiceImpl openAi,
            GptJServiceImpl gptj
    ) {
        return switch (provider.toLowerCase()) {
            case "openai" -> openAi;
            case "gptj" -> gptj;
            default -> throw new IllegalArgumentException("Unknown AI provider: " + provider);
        };
    }
}

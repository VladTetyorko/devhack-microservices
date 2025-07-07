package com.vladte.devhack.parser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for the parser module.
 */
@Configuration
@EnableScheduling
public class ParserConfig {

    /**
     * Create a RestTemplate bean for making HTTP requests.
     *
     * @return The RestTemplate bean
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
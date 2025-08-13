package com.vladte.devhack.parser.service;

import com.vladte.devhack.entities.global.Vacancy;

import java.util.List;
import java.util.Map;

/**
 * Generic interface for vacancy provider services that query external APIs.
 * Implementations should handle specific job websites' APIs.
 */
public interface VacancyProviderService {

    /**
     * Authorize with the vacancy provider's API.
     * This method should handle authentication/authorization logic specific to each provider.
     *
     * @param credentials Map containing authentication credentials (API keys, tokens, etc.)
     * @return true if authorization was successful, false otherwise
     */
    boolean authorize(Map<String, String> credentials);

    /**
     * Query the vacancy provider's API for job vacancies.
     *
     * @param queryParams Map containing query parameters (location, keywords, etc.)
     * @return Raw response from the API (JSON, XML, etc.)
     */
    String query(Map<String, String> queryParams);

    /**
     * Parse the raw API response to extract job vacancies.
     *
     * @param apiResponse The raw API response to parse
     * @return A list of extracted vacancies
     */
    List<Vacancy> parse(String apiResponse);

    /**
     * Get the name of the vacancy provider (e.g., "Djinny", "DOU", "LinkedIn").
     *
     * @return The provider name
     */
    String getProviderName();

    /**
     * Check if the service is currently authorized.
     *
     * @return true if authorized, false otherwise
     */
    boolean isAuthorized();

    /**
     * Get the base URL for the provider's API.
     *
     * @return The base API URL
     */
    String getApiBaseUrl();
}
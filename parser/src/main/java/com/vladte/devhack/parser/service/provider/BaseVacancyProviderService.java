package com.vladte.devhack.parser.service.provider;

import com.vladte.devhack.entities.global.Vacancy;
import com.vladte.devhack.parser.service.VacancyProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Base abstract implementation of VacancyProviderService providing common functionality.
 * Child services should extend this class and override specific methods as needed.
 */
@Slf4j
public abstract class BaseVacancyProviderService implements VacancyProviderService {

    protected final RestTemplate restTemplate;
    protected boolean authorized = false;
    protected Map<String, String> authCredentials;

    protected BaseVacancyProviderService() {
        this.restTemplate = new RestTemplate();
    }

    protected BaseVacancyProviderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Default authorization implementation.
     * Child classes should override this method with provider-specific logic.
     *
     * @param credentials Map containing authentication credentials
     * @return true if authorization was successful, false otherwise
     */
    @Override
    public boolean authorize(Map<String, String> credentials) {
        log.info("Attempting to authorize with {} provider", getProviderName());

        if (credentials == null || credentials.isEmpty()) {
            log.warn("No credentials provided for {} provider", getProviderName());
            return false;
        }

        try {
            // Store credentials for later use
            this.authCredentials = credentials;

            // Perform provider-specific authorization
            boolean authResult = performAuthorization(credentials);

            if (authResult) {
                this.authorized = true;
                log.info("Successfully authorized with {} provider", getProviderName());
            } else {
                log.error("Failed to authorize with {} provider", getProviderName());
            }

            return authResult;
        } catch (Exception e) {
            log.error("Error during authorization with {} provider: {}", getProviderName(), e.getMessage());
            return false;
        }
    }

    /**
     * Default query implementation using REST template.
     * Child classes can override this method for provider-specific query logic.
     *
     * @param queryParams Map containing query parameters
     * @return Raw response from the API
     */
    @Override
    public String query(Map<String, String> queryParams) {
        log.info("Querying {} provider with parameters: {}", getProviderName(), queryParams);

        if (!isAuthorized()) {
            log.error("Not authorized to query {} provider", getProviderName());
            return null;
        }

        try {
            String url = buildQueryUrl(queryParams);
            HttpHeaders headers = buildHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully queried {} provider", getProviderName());
                return response.getBody();
            } else {
                log.error("Failed to query {} provider. Status: {}",
                        getProviderName(), response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error querying {} provider: {}", getProviderName(), e.getMessage());
            return null;
        }
    }

    /**
     * Abstract method for parsing API response.
     * Child classes must implement this method with provider-specific parsing logic.
     *
     * @param apiResponse The raw API response to parse
     * @return A list of extracted vacancies
     */
    @Override
    public abstract List<Vacancy> parse(String apiResponse);

    /**
     * Abstract method for getting provider name.
     * Child classes must implement this method.
     *
     * @return The provider name
     */
    @Override
    public abstract String getProviderName();

    /**
     * Abstract method for getting API base URL.
     * Child classes must implement this method.
     *
     * @return The base API URL
     */
    @Override
    public abstract String getApiBaseUrl();

    /**
     * Check if the service is currently authorized.
     *
     * @return true if authorized, false otherwise
     */
    @Override
    public boolean isAuthorized() {
        return authorized;
    }

    /**
     * Perform provider-specific authorization logic.
     * Child classes should override this method.
     *
     * @param credentials Map containing authentication credentials
     * @return true if authorization was successful, false otherwise
     */
    protected abstract boolean performAuthorization(Map<String, String> credentials);

    /**
     * Build the query URL with parameters.
     * Child classes can override this method for provider-specific URL building.
     *
     * @param queryParams Map containing query parameters
     * @return The complete query URL
     */
    protected String buildQueryUrl(Map<String, String> queryParams) {
        StringBuilder url = new StringBuilder(getApiBaseUrl());

        if (queryParams != null && !queryParams.isEmpty()) {
            url.append("?");
            queryParams.forEach((key, value) ->
                    url.append(key).append("=").append(value).append("&")
            );
            // Remove trailing &
            url.setLength(url.length() - 1);
        }

        return url.toString();
    }

    /**
     * Build HTTP headers for API requests.
     * Child classes can override this method for provider-specific headers.
     *
     * @return HttpHeaders object with necessary headers
     */
    protected HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "DevHack-VacancyParser/1.0");
        headers.set("Accept", "application/json");

        // Add authorization headers if available
        if (authCredentials != null) {
            addAuthorizationHeaders(headers);
        }

        return headers;
    }

    /**
     * Add authorization headers to the request.
     * Child classes should override this method with provider-specific auth headers.
     *
     * @param headers HttpHeaders object to add authorization headers to
     */
    protected void addAuthorizationHeaders(HttpHeaders headers) {
        // Default implementation - child classes should override
        if (authCredentials.containsKey("apiKey")) {
            headers.set("Authorization", "Bearer " + authCredentials.get("apiKey"));
        }
    }

    /**
     * Create a basic Vacancy object with common fields set.
     * Child classes can use this as a starting point and add provider-specific fields.
     *
     * @return A new Vacancy object with basic fields set
     */
    protected Vacancy createBaseVacancy() {
        Vacancy vacancy = new Vacancy();
        vacancy.setSource(getProviderName());
        vacancy.setCreatedAt(LocalDateTime.now());
        vacancy.setUpdatedAt(LocalDateTime.now());
        return vacancy;
    }

    /**
     * Reset authorization state.
     * Useful for testing or when credentials change.
     */
    protected void resetAuthorization() {
        this.authorized = false;
        this.authCredentials = null;
        log.info("Authorization reset for {} provider", getProviderName());
    }
}
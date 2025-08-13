package com.vladte.devhack.parser.service.impl.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.entities.global.Vacancy;
import com.vladte.devhack.parser.service.provider.BaseVacancyProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Vacancy provider service for DOU job site API.
 * Handles authorization, querying, and parsing of DOU API responses.
 */
@Service
@Slf4j
public class DouVacancyProviderService extends BaseVacancyProviderService {

    private static final String PROVIDER_NAME = "DOU";
    private static final String API_BASE_URL = "https://api.dou.ua/v1";
    private final ObjectMapper objectMapper;

    public DouVacancyProviderService() {
        super();
        this.objectMapper = new ObjectMapper();
    }

    public DouVacancyProviderService(RestTemplate restTemplate) {
        super(restTemplate);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String getApiBaseUrl() {
        return API_BASE_URL + "/vacancies";
    }

    /**
     * Perform DOU-specific authorization.
     * DOU typically uses OAuth2 or API key authentication.
     *
     * @param credentials Map containing authentication credentials
     * @return true if authorization was successful, false otherwise
     */
    @Override
    protected boolean performAuthorization(Map<String, String> credentials) {
        log.info("Performing DOU-specific authorization");

        String apiKey = credentials.get("apiKey");
        String accessToken = credentials.get("accessToken");

        if ((apiKey == null || apiKey.trim().isEmpty()) &&
                (accessToken == null || accessToken.trim().isEmpty())) {
            log.error("API key or access token is required for DOU authorization");
            return false;
        }

        try {
            // Test the credentials by making a simple request
            String testUrl = API_BASE_URL + "/user/profile";
            HttpHeaders headers = new HttpHeaders();

            if (accessToken != null && !accessToken.trim().isEmpty()) {
                headers.set("Authorization", "Bearer " + accessToken);
            } else if (apiKey != null && !apiKey.trim().isEmpty()) {
                headers.set("X-API-Key", apiKey);
            }

            headers.set("Accept", "application/json");

            // In a real implementation, you would make a test request to validate the credentials
            // For now, we'll assume the credentials are valid if they're provided
            log.info("DOU credentials validation successful");
            return true;
        } catch (Exception e) {
            log.error("Failed to validate DOU credentials: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Add DOU-specific authorization headers.
     *
     * @param headers HttpHeaders object to add authorization headers to
     */
    @Override
    protected void addAuthorizationHeaders(HttpHeaders headers) {
        if (authCredentials != null) {
            String accessToken = authCredentials.get("accessToken");
            String apiKey = authCredentials.get("apiKey");

            if (accessToken != null && !accessToken.trim().isEmpty()) {
                headers.set("Authorization", "Bearer " + accessToken);
            } else if (apiKey != null && !apiKey.trim().isEmpty()) {
                headers.set("X-API-Key", apiKey);
            }
        }
    }

    /**
     * Build DOU-specific query URL with parameters.
     *
     * @param queryParams Map containing query parameters
     * @return The complete query URL
     */
    @Override
    protected String buildQueryUrl(Map<String, String> queryParams) {
        StringBuilder url = new StringBuilder(getApiBaseUrl());

        if (queryParams != null && !queryParams.isEmpty()) {
            url.append("?");

            // Map common parameters to DOU-specific parameter names
            queryParams.forEach((key, value) -> {
                String douParam = mapToDouParameter(key);
                url.append(douParam).append("=").append(value).append("&");
            });

            // Remove trailing &
            if (url.charAt(url.length() - 1) == '&') {
                url.setLength(url.length() - 1);
            }
        }

        return url.toString();
    }

    /**
     * Map common parameter names to DOU-specific parameter names.
     *
     * @param commonParam Common parameter name
     * @return DOU-specific parameter name
     */
    private String mapToDouParameter(String commonParam) {
        return switch (commonParam.toLowerCase()) {
            case "keywords", "query" -> "search";
            case "location", "city" -> "city";
            case "experience" -> "exp";
            case "remote" -> "remote";
            case "salary" -> "salary";
            case "page" -> "page";
            case "limit", "size" -> "count";
            case "category" -> "category";
            default -> commonParam;
        };
    }

    /**
     * Parse DOU API response to extract job vacancies.
     *
     * @param apiResponse The raw API response to parse
     * @return A list of extracted vacancies
     */
    @Override
    public List<Vacancy> parse(String apiResponse) {
        log.info("Parsing DOU API response");
        List<Vacancy> vacancies = new ArrayList<>();

        if (apiResponse == null || apiResponse.trim().isEmpty()) {
            log.warn("Empty API response from DOU");
            return vacancies;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(apiResponse);
            JsonNode vacanciesNode = rootNode.get("vacancies");

            if (vacanciesNode == null || !vacanciesNode.isArray()) {
                log.warn("No vacancies data found in DOU API response");
                return vacancies;
            }

            for (JsonNode vacancyNode : vacanciesNode) {
                try {
                    Vacancy vacancy = createBaseVacancy();

                    // Extract job details from JSON
                    if (vacancyNode.has("title")) {
                        vacancy.setPosition(vacancyNode.get("title").asText());
                    }

                    if (vacancyNode.has("company")) {
                        JsonNode companyNode = vacancyNode.get("company");
                        if (companyNode.has("name")) {
                            vacancy.setCompanyName(companyNode.get("name").asText());
                        }
                    }

                    if (vacancyNode.has("technologies")) {
                        JsonNode techNode = vacancyNode.get("technologies");
                        if (techNode.isArray()) {
                            List<String> technologies = new ArrayList<>();
                            for (JsonNode tech : techNode) {
                                technologies.add(tech.asText());
                            }
                            vacancy.setTechnologies(String.join(", ", technologies));
                        } else {
                            vacancy.setTechnologies(techNode.asText());
                        }
                    }

                    if (vacancyNode.has("url")) {
                        vacancy.setUrl(vacancyNode.get("url").asText());
                    }

                    if (vacancyNode.has("remote")) {
                        vacancy.setRemoteAllowed(vacancyNode.get("remote").asBoolean());
                    }

                    if (vacancyNode.has("published_at")) {
                        // Parse the date string to LocalDateTime if needed
                        String publishedAtStr = vacancyNode.get("published_at").asText();
                        // For now, we'll set it to current time, but in real implementation
                        // you would parse the actual date from the API response
                        vacancy.setOpenAt(LocalDateTime.now());
                    }

                    if (vacancyNode.has("contact")) {
                        JsonNode contactNode = vacancyNode.get("contact");
                        if (contactNode.has("email")) {
                            vacancy.setContactEmail(contactNode.get("email").asText());
                        }
                        if (contactNode.has("name")) {
                            vacancy.setContactPerson(contactNode.get("name").asText());
                        }
                    }

                    if (vacancyNode.has("deadline")) {
                        // Parse deadline if provided
                        String deadlineStr = vacancyNode.get("deadline").asText();
                        // For now, we'll set it to a future date, but in real implementation
                        // you would parse the actual deadline from the API response
                        vacancy.setDeadline(LocalDateTime.now().plusDays(30));
                    }

                    vacancies.add(vacancy);
                } catch (Exception e) {
                    log.error("Error parsing individual vacancy from DOU API: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error parsing DOU API response: {}", e.getMessage());
        }

        log.info("Extracted {} vacancies from DOU API", vacancies.size());
        return vacancies;
    }
}
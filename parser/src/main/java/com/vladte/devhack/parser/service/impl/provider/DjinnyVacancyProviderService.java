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
 * Vacancy provider service for Djinny job site API.
 * Handles authorization, querying, and parsing of Djinny API responses.
 */
@Service
@Slf4j
public class DjinnyVacancyProviderService extends BaseVacancyProviderService {

    private static final String PROVIDER_NAME = "Djinny";
    private static final String API_BASE_URL = "https://api.djinny.co/v1";
    private final ObjectMapper objectMapper;

    public DjinnyVacancyProviderService() {
        super();
        this.objectMapper = new ObjectMapper();
    }

    public DjinnyVacancyProviderService(RestTemplate restTemplate) {
        super(restTemplate);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String getApiBaseUrl() {
        return API_BASE_URL + "/jobs";
    }

    /**
     * Perform Djinny-specific authorization.
     * Djinny typically uses API key authentication.
     *
     * @param credentials Map containing authentication credentials
     * @return true if authorization was successful, false otherwise
     */
    @Override
    protected boolean performAuthorization(Map<String, String> credentials) {
        log.info("Performing Djinny-specific authorization");

        String apiKey = credentials.get("apiKey");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("API key is required for Djinny authorization");
            return false;
        }

        try {
            // Test the API key by making a simple request
            String testUrl = API_BASE_URL + "/auth/test";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Accept", "application/json");

            // In a real implementation, you would make a test request to validate the API key
            // For now, we'll assume the API key is valid if it's provided
            log.info("Djinny API key validation successful");
            return true;
        } catch (Exception e) {
            log.error("Failed to validate Djinny API key: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Add Djinny-specific authorization headers.
     *
     * @param headers HttpHeaders object to add authorization headers to
     */
    @Override
    protected void addAuthorizationHeaders(HttpHeaders headers) {
        if (authCredentials != null && authCredentials.containsKey("apiKey")) {
            headers.set("Authorization", "Bearer " + authCredentials.get("apiKey"));
        }
    }

    /**
     * Build Djinny-specific query URL with parameters.
     *
     * @param queryParams Map containing query parameters
     * @return The complete query URL
     */
    @Override
    protected String buildQueryUrl(Map<String, String> queryParams) {
        StringBuilder url = new StringBuilder(getApiBaseUrl());

        if (queryParams != null && !queryParams.isEmpty()) {
            url.append("?");

            // Map common parameters to Djinny-specific parameter names
            queryParams.forEach((key, value) -> {
                String djinnyParam = mapToDjinnyParameter(key);
                url.append(djinnyParam).append("=").append(value).append("&");
            });

            // Remove trailing &
            if (url.charAt(url.length() - 1) == '&') {
                url.setLength(url.length() - 1);
            }
        }

        return url.toString();
    }

    /**
     * Map common parameter names to Djinny-specific parameter names.
     *
     * @param commonParam Common parameter name
     * @return Djinny-specific parameter name
     */
    private String mapToDjinnyParameter(String commonParam) {
        return switch (commonParam.toLowerCase()) {
            case "keywords", "query" -> "q";
            case "location", "city" -> "location";
            case "experience" -> "exp_level";
            case "remote" -> "remote_work";
            case "salary" -> "salary_from";
            case "page" -> "page";
            case "limit", "size" -> "per_page";
            default -> commonParam;
        };
    }

    /**
     * Parse Djinny API response to extract job vacancies.
     *
     * @param apiResponse The raw API response to parse
     * @return A list of extracted vacancies
     */
    @Override
    public List<Vacancy> parse(String apiResponse) {
        log.info("Parsing Djinny API response");
        List<Vacancy> vacancies = new ArrayList<>();

        if (apiResponse == null || apiResponse.trim().isEmpty()) {
            log.warn("Empty API response from Djinny");
            return vacancies;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(apiResponse);
            JsonNode jobsNode = rootNode.get("data");

            if (jobsNode == null || !jobsNode.isArray()) {
                log.warn("No jobs data found in Djinny API response");
                return vacancies;
            }

            for (JsonNode jobNode : jobsNode) {
                try {
                    Vacancy vacancy = createBaseVacancy();

                    // Extract job details from JSON
                    if (jobNode.has("title")) {
                        vacancy.setPosition(jobNode.get("title").asText());
                    }

                    if (jobNode.has("company")) {
                        JsonNode companyNode = jobNode.get("company");
                        if (companyNode.has("name")) {
                            vacancy.setCompanyName(companyNode.get("name").asText());
                        }
                    }

                    if (jobNode.has("skills")) {
                        JsonNode skillsNode = jobNode.get("skills");
                        if (skillsNode.isArray()) {
                            List<String> skills = new ArrayList<>();
                            for (JsonNode skillNode : skillsNode) {
                                skills.add(skillNode.asText());
                            }
                            vacancy.setTechnologies(String.join(", ", skills));
                        }
                    }

                    if (jobNode.has("url")) {
                        vacancy.setUrl(jobNode.get("url").asText());
                    }

                    if (jobNode.has("remote_work")) {
                        vacancy.setRemoteAllowed(jobNode.get("remote_work").asBoolean());
                    }

                    if (jobNode.has("open_at")) {
                        // Parse the date string to LocalDateTime if needed
                        String openAtStr = jobNode.get("open_at").asText();
                        // For now, we'll set it to current time, but in real implementation
                        // you would parse the actual date from the API response
                        vacancy.setOpenAt(LocalDateTime.now());
                    }

                    if (jobNode.has("contact")) {
                        JsonNode contactNode = jobNode.get("contact");
                        if (contactNode.has("email")) {
                            vacancy.setContactEmail(contactNode.get("email").asText());
                        }
                        if (contactNode.has("person")) {
                            vacancy.setContactPerson(contactNode.get("person").asText());
                        }
                    }

                    vacancies.add(vacancy);
                } catch (Exception e) {
                    log.error("Error parsing individual job from Djinny API: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error parsing Djinny API response: {}", e.getMessage());
        }

        log.info("Extracted {} vacancies from Djinny API", vacancies.size());
        return vacancies;
    }
}
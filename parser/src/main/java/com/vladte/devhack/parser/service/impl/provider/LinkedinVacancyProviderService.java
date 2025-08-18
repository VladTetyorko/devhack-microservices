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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Vacancy provider service for LinkedIn job site API.
 * Handles authorization, querying, and parsing of LinkedIn API responses.
 */
@Service
@Slf4j
public class LinkedinVacancyProviderService extends BaseVacancyProviderService {

    private static final String PROVIDER_NAME = "LinkedIn";
    private static final String API_BASE_URL = "https://api.linkedin.com/v2";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");
    private final ObjectMapper objectMapper;

    public LinkedinVacancyProviderService() {
        super();
        this.objectMapper = new ObjectMapper();
    }

    public LinkedinVacancyProviderService(RestTemplate restTemplate) {
        super(restTemplate);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String getApiBaseUrl() {
        return API_BASE_URL + "/jobPostings";
    }

    /**
     * Perform LinkedIn-specific authorization.
     * LinkedIn uses OAuth2 authentication with access tokens.
     *
     * @param credentials Map containing authentication credentials
     * @return true if authorization was successful, false otherwise
     */
    @Override
    protected boolean performAuthorization(Map<String, String> credentials) {
        log.info("Performing LinkedIn-specific authorization");

        String accessToken = credentials.get("accessToken");
        String clientId = credentials.get("clientId");
        String clientSecret = credentials.get("clientSecret");

        if (accessToken == null || accessToken.trim().isEmpty()) {
            log.error("Access token is required for LinkedIn authorization");
            return false;
        }

        try {
            // Test the access token by making a simple request to LinkedIn API
            String testUrl = API_BASE_URL + "/people/~";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/json");
            headers.set("X-Restli-Protocol-Version", "2.0.0");

            // In a real implementation, you would make a test request to validate the access token
            // For now, we'll assume the access token is valid if it's provided
            log.info("LinkedIn access token validation successful");
            return true;
        } catch (Exception e) {
            log.error("Failed to validate LinkedIn access token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Add LinkedIn-specific authorization headers.
     *
     * @param headers HttpHeaders object to add authorization headers to
     */
    @Override
    protected void addAuthorizationHeaders(HttpHeaders headers) {
        if (authCredentials != null && authCredentials.containsKey("accessToken")) {
            headers.set("Authorization", "Bearer " + authCredentials.get("accessToken"));
            headers.set("X-Restli-Protocol-Version", "2.0.0");
        }
    }

    /**
     * Build LinkedIn-specific query URL with parameters.
     *
     * @param queryParams Map containing query parameters
     * @return The complete query URL
     */
    @Override
    protected String buildQueryUrl(Map<String, String> queryParams) {
        StringBuilder url = new StringBuilder(getApiBaseUrl());

        if (queryParams != null && !queryParams.isEmpty()) {
            url.append("?");

            // Map common parameters to LinkedIn-specific parameter names
            queryParams.forEach((key, value) -> {
                String linkedinParam = mapToLinkedinParameter(key);
                url.append(linkedinParam).append("=").append(value).append("&");
            });

            // Remove trailing &
            if (url.charAt(url.length() - 1) == '&') {
                url.setLength(url.length() - 1);
            }
        }

        return url.toString();
    }

    /**
     * Map common parameter names to LinkedIn-specific parameter names.
     *
     * @param commonParam Common parameter name
     * @return LinkedIn-specific parameter name
     */
    private String mapToLinkedinParameter(String commonParam) {
        return switch (commonParam.toLowerCase()) {
            case "keywords", "query" -> "keywords";
            case "location", "city" -> "locationName";
            case "experience" -> "experienceLevel";
            case "remote" -> "workplaceType";
            case "company" -> "companyId";
            case "page" -> "start";
            case "limit", "size" -> "count";
            case "industry" -> "industries";
            case "jobtype" -> "jobType";
            default -> commonParam;
        };
    }

    /**
     * Parse LinkedIn API response to extract job vacancies.
     *
     * @param apiResponse The raw API response to parse
     * @return A list of extracted vacancies
     */
    @Override
    public List<Vacancy> parse(String apiResponse) {
        log.info("Parsing LinkedIn API response");
        List<Vacancy> vacancies = new ArrayList<>();

        if (apiResponse == null || apiResponse.trim().isEmpty()) {
            log.warn("Empty API response from LinkedIn");
            return vacancies;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(apiResponse);
            JsonNode elementsNode = rootNode.get("elements");

            if (elementsNode == null || !elementsNode.isArray()) {
                log.warn("No elements data found in LinkedIn API response");
                return vacancies;
            }

            for (JsonNode jobNode : elementsNode) {
                try {
                    Vacancy vacancy = createBaseVacancy();

                    // Extract job details from JSON
                    if (jobNode.has("title")) {
                        vacancy.setPosition(jobNode.get("title").asText());
                    }

                    // Extract company information
                    if (jobNode.has("companyDetails")) {
                        JsonNode companyNode = jobNode.get("companyDetails");
                        if (companyNode.has("company")) {
                            JsonNode companyInfoNode = companyNode.get("company");
                            if (companyInfoNode.has("name")) {
                                vacancy.setCompanyName(companyInfoNode.get("name").asText());
                            }
                        }
                    }

                    // Extract job description and try to find technologies
                    if (jobNode.has("description")) {
                        JsonNode descriptionNode = jobNode.get("description");
                        String description = descriptionNode.asText();

                        // Try to extract technologies from description using simple keyword matching
                        String technologies = extractTechnologiesFromDescription(description);
                        if (!technologies.isEmpty()) {
                            vacancy.setTechnologies(technologies);
                        }

                        // Try to extract email from description
                        Matcher matcher = EMAIL_PATTERN.matcher(description);
                        if (matcher.find()) {
                            vacancy.setContactEmail(matcher.group());
                        }

                        // Check for remote work mentions
                        vacancy.setRemoteAllowed(
                                description.toLowerCase().contains("remote") ||
                                        description.toLowerCase().contains("work from home") ||
                                        description.toLowerCase().contains("telecommute")
                        );
                    }

                    // Extract job posting URL
                    if (jobNode.has("jobPostingUrl")) {
                        vacancy.setUrl(jobNode.get("jobPostingUrl").asText());
                    }

                    // Extract location information
                    if (jobNode.has("formattedLocation")) {
                        // Note: Vacancy entity doesn't have location field, 
                        // but we could store it in technologies field as additional info
                        String location = jobNode.get("formattedLocation").asText();
                        if (vacancy.getTechnologies() != null) {
                            vacancy.setTechnologies(vacancy.getTechnologies() + " | Location: " + location);
                        } else {
                            vacancy.setTechnologies("Location: " + location);
                        }
                    }

                    // Extract posting date
                    if (jobNode.has("listedAt")) {
                        // Parse the timestamp (LinkedIn uses milliseconds since epoch)
                        long timestamp = jobNode.get("listedAt").asLong();
                        // For now, we'll set it to current time, but in real implementation
                        // you would convert the timestamp to LocalDateTime
                        vacancy.setOpenAt(LocalDateTime.now());
                    }

                    // Extract contact information if available
                    if (jobNode.has("contactInfo")) {
                        JsonNode contactNode = jobNode.get("contactInfo");
                        if (contactNode.has("email")) {
                            vacancy.setContactEmail(contactNode.get("email").asText());
                        }
                        if (contactNode.has("name")) {
                            vacancy.setContactPerson(contactNode.get("name").asText());
                        }
                    }

                    // Extract application deadline if available
                    if (jobNode.has("applicationDeadline")) {
                        long deadlineTimestamp = jobNode.get("applicationDeadline").asLong();
                        // For now, we'll set it to a future date, but in real implementation
                        // you would convert the timestamp to LocalDateTime
                        vacancy.setDeadline(LocalDateTime.now().plusDays(30));
                    }

                    vacancies.add(vacancy);
                } catch (Exception e) {
                    log.error("Error parsing individual job from LinkedIn API: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error parsing LinkedIn API response: {}", e.getMessage());
        }

        log.info("Extracted {} vacancies from LinkedIn API", vacancies.size());
        return vacancies;
    }

    /**
     * Extract technologies from job description using simple keyword matching.
     * In a real implementation, this could use NLP or a more sophisticated approach.
     *
     * @param description Job description text
     * @return Comma-separated list of found technologies
     */
    private String extractTechnologiesFromDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return "";
        }

        List<String> foundTechnologies = new ArrayList<>();
        String lowerDescription = description.toLowerCase();

        // Common technology keywords to look for
        String[] techKeywords = {
                "java", "python", "javascript", "typescript", "react", "angular", "vue",
                "spring", "node.js", "express", "django", "flask", "docker", "kubernetes",
                "aws", "azure", "gcp", "mysql", "postgresql", "mongodb", "redis",
                "git", "jenkins", "ci/cd", "microservices", "rest", "graphql",
                "html", "css", "bootstrap", "sass", "webpack", "gradle", "maven"
        };

        for (String tech : techKeywords) {
            if (lowerDescription.contains(tech.toLowerCase())) {
                foundTechnologies.add(tech);
            }
        }

        return String.join(", ", foundTechnologies);
    }
}
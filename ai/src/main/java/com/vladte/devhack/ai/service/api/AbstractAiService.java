package com.vladte.devhack.ai.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.entities.VacancyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract base class for AI service implementations.
 * This class provides common functionality for different AI service implementations.
 * It follows SOLID principles with separation of concerns between API communication,
 * response parsing, and business logic.
 */
public abstract class AbstractAiService implements OpenAiService {

    private static final Logger log = LoggerFactory.getLogger(AbstractAiService.class);

    protected final RestTemplate restTemplate;
    protected final WebClient webClient;

    // Abstract methods for configuration
    protected abstract String getApiKey();

    protected abstract String getModel();

    protected abstract int getMaxTokens();

    protected abstract String getApiUrl();

    protected AbstractAiService() {
        this.restTemplate = new RestTemplate();
        this.webClient = WebClient.builder().build();
        log.debug("Initialized AbstractAiService with default RestTemplate and WebClient");
    }

    // Core text generation methods
    private String generateText(String prompt) {
        log.debug("Generating text with prompt length: {}", prompt.length());
        try {
            return executeTextGeneration(prompt);
        } catch (Exception e) {
            return handleTextGenerationError(e);
        }
    }

    private String executeTextGeneration(String prompt) {
        HttpEntity<Map<String, Object>> request = createApiRequest(prompt);
        Map<String, Object> responseBody = callApi(request);
        return parseApiResponse(responseBody);
    }

    // API Request/Response handling
    protected HttpEntity<Map<String, Object>> createApiRequest(String prompt) {
        log.debug("Creating API request for model: {}", getModel());
        HttpHeaders headers = createRequestHeaders();
        Map<String, Object> requestBody = createRequestBody(prompt);
        return new HttpEntity<>(requestBody, headers);
    }

    private HttpHeaders createRequestHeaders() {
        log.debug("Creating request headers with API key");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getApiKey());
        return headers;
    }

    private Map<String, Object> createRequestBody(String prompt) {
        log.debug("Creating request body with prompt length: {}", prompt.length());
        Map<String, Object> message = createUserMessage(prompt);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", getModel());
        requestBody.put("messages", List.of(message));
        return requestBody;
    }

    private Map<String, Object> createUserMessage(String prompt) {
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        return message;
    }

    // API Communication 
    protected Map<String, Object> callApi(HttpEntity<Map<String, Object>> request) throws RestClientException {
        log.debug("Calling AI API at: {}", getApiUrl());
        try {
            ResponseEntity<Map> response = executeApiCall(request);
            log.debug("API call successful, status: {}", response.getStatusCode());
            return response.getBody();
        } catch (RestClientException e) {
            handleApiCallException(e);
            throw e;
        }
    }

    private ResponseEntity<Map> executeApiCall(HttpEntity<Map<String, Object>> request) throws RestClientException {
        return restTemplate.postForEntity(getApiUrl(), request, Map.class);
    }

    private void handleApiCallException(Exception e) {
        if (e instanceof HttpClientErrorException) {
            log.error("Client error calling AI API: {}", e.getMessage(), e);
        } else if (e instanceof HttpServerErrorException) {
            log.error("Server error from AI API: {}", e.getMessage(), e);
        } else if (e instanceof RestClientException) {
            log.error("Error communicating with AI API: {}", e.getMessage(), e);
        } else {
            log.error("Unexpected error during API call: {}", e.getMessage(), e);
        }
    }

    // Response Parsing
    protected String parseApiResponse(Map<String, Object> responseBody) {
        log.debug("Parsing API response");
        String validationError = validateResponseBody(responseBody);
        if (validationError != null) {
            return validationError;
        }
        Map<String, Object> choice = extractChoiceFromResponse(responseBody);
        if (choice == null) {
            return "Failed to generate text: no valid choice found";
        }
        return extractContentFromChoice(choice);
    }

    private String validateResponseBody(Map<String, Object> responseBody) {
        System.out.println("responce body = " + responseBody);
        if (responseBody == null) {
            log.warn("Received null response body");
            return "Failed to generate text: null response";
        }
        if (!responseBody.containsKey("choices")) {
            log.warn("Response does not contain 'choices' field: {}", responseBody);
            return "Failed to generate text: invalid response format";
        }
        return null;
    }

    private Map<String, Object> extractChoiceFromResponse(Map<String, Object> responseBody) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        if (choices.isEmpty()) {
            log.warn("Response contains empty choices list");
            return null;
        }
        Map<String, Object> choice = choices.get(0);
        if (!choice.containsKey("message")) {
            log.warn("First choice does not contain 'message' field: {}", choice);
            return null;
        }
        return choice;
    }

    private String extractContentFromChoice(Map<String, Object> choice) {
        Map<String, Object> messageResponse = (Map<String, Object>) choice.get("message");
        if (!messageResponse.containsKey("content")) {
            log.warn("Message does not contain 'content' field: {}", messageResponse);
            return "Failed to generate text: invalid message format";
        }
        String content = (String) messageResponse.get("content");
        log.debug("Successfully parsed response, content length: {}", content.length());
        return content;
    }

    // Async Operations
    @Async
    protected CompletableFuture<Map<String, Object>> callApiAsync(HttpEntity<Map<String, Object>> request) {
        log.debug("Calling AI API asynchronously at: {}", getApiUrl());
        return executeApiCallAsync(request)
                .thenApply(response -> {
                    log.debug("Async API call successful");
                    return response;
                })
                .exceptionally(e -> {
                    if (e instanceof Exception) {
                        handleApiCallException((Exception) e);
                    } else {
                        log.error("Unexpected error type during async API call: {}", e.getMessage(), e);
                    }
                    throw new CompletionException(e);
                });
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<Map<String, Object>> executeApiCallAsync(HttpEntity<Map<String, Object>> request) {
        return webClient.post()
                .uri(getApiUrl())
                .headers(headers -> {
                    headers.addAll(request.getHeaders());
                })
                .bodyValue(request.getBody())
                .retrieve()
                .bodyToMono(Map.class)
                .map(map -> (Map<String, Object>) map)
                .toFuture();
    }

    private CompletableFuture<String> executeTextGenerationAsync(String prompt) {
        log.debug("Executing text generation asynchronously with prompt length: {}", prompt.length());
        try {
            HttpEntity<Map<String, Object>> request = createApiRequest(prompt);
            return callApiAsync(request)
                    .thenApply(responseBody -> {
                        try {
                            return parseApiResponse(responseBody);
                        } catch (Exception e) {
                            log.error("Error parsing API response: {}", e.getMessage(), e);
                            throw new CompletionException(e);
                        }
                    });
        } catch (Exception e) {
            log.error("Error preparing async API request: {}", e.getMessage(), e);
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    // Public Interface Methods  
    @Override
    @Async
    public CompletableFuture<String> generateTextAsync(String prompt) {
        log.debug("Generating text asynchronously with prompt length: {}", prompt.length());
        try {
            return executeTextGenerationAsync(prompt);
        } catch (Exception e) {
            log.error("Error in async text generation: {}", e.getMessage(), e);
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    @Async
    public CompletableFuture<String> generateQuestionsForTagAsync(String tag, int count, String difficulty) {
        log.debug("Generating questions asynchronously for tag '{}' at {} difficulty", tag, difficulty);
        String prompt = createQuestionGenerationPrompt(tag, count, difficulty);
        return generateTextAsync(prompt);
    }

    @Override
    @Async
    public CompletableFuture<Boolean> checkAnswerForCheatingAsync(String questionText, String answerText) {
        log.debug("Checking if answer contains cheating asynchronously for question: '{}'", questionText);
        String prompt = createAnswerCheckForCheatingPrompt(questionText, answerText);
        return generateTextAsync(prompt)
                .thenApply(this::extractCheatingResultFromResponse);
    }

    @Override
    @Async
    public CompletableFuture<Map<String, Object>> checkAnswerWithFeedbackAsync(String questionText, String answerText) {
        log.debug("Checking answer with feedback asynchronously for question: '{}'", questionText);
        String prompt = createAnswerCheckWithFeedbackPrompt(questionText, answerText);
        return generateTextAsync(prompt)
                .thenApply(this::extractScoreAndFeedbackFromResponse);
    }

    @Override
    @Async
    public CompletableFuture<Map<String, Object>> extractVacancyModelFromDescription(String vacancyDescription) {
        log.debug("Extracting vacancy model from description asynchronously for vacancy description: '{}'", vacancyDescription);
        String prompt = createVacancyDescriptionPrompt(vacancyDescription);
        return generateTextAsync(prompt)
                .thenApply(this::extractVacancyModelFromResponse);
    }

    // Helper Methods
    protected String createQuestionGenerationPrompt(String tag, int count, String difficulty) {
        return String.format(
                AiPromptConstraints.GENERATE_QUESTIONS_TEMPLATE,
                "Java",
                count, tag, difficulty, tag);
    }

    protected String createAnswerCheckForCheatingPrompt(String questionText, String answerText) {
        return String.format(
                AiPromptConstraints.CHECK_ANSWER_FOR_CHEATING_TEMPLATE,
                questionText, answerText);
    }

    protected String createAnswerCheckWithFeedbackPrompt(String questionText, String answerText) {
        return String.format(
                AiPromptConstraints.CHECK_ANSWER_WITH_FEEDBACK_TEMPLATE,
                questionText, answerText);
    }

    protected String createVacancyDescriptionPrompt(String vacancyDescription) {
        return String.format(
                AiPromptConstraints.PARSE_VACANCY_DESCRIPTION,
                vacancyDescription);
    }

    private String handleTextGenerationError(Exception e) {
        log.error("Error generating text: {}", e.getMessage(), e);
        return "Error generating text: " + e.getMessage();
    }

    protected Boolean extractCheatingResultFromResponse(String response) {
        log.debug("Extracting cheating result from response");
        if (response == null || response.isEmpty()) {
            log.warn("Empty response received when extracting cheating result");
            return false;
        }
        try {
            String trimmedResponse = response.trim().toLowerCase();
            return "true".equals(trimmedResponse);
        } catch (Exception e) {
            log.error("Error extracting cheating result from response: {}", e.getMessage(), e);
            return false;
        }
    }

    protected Map<String, Object> extractScoreAndFeedbackFromResponse(String response) {
        log.debug("Extracting score and feedback from response");
        if (response == null || response.isEmpty()) {
            return createEmptyResponseResult();
        }
        try {
            return extractScoreAndFeedback(response);
        } catch (Exception e) {
            return handleExtractionError(e);
        }
    }

    private Map<String, Object> createEmptyResponseResult() {
        log.warn("Empty response received when extracting score and feedback");
        Map<String, Object> result = new HashMap<>();
        result.put("score", 0.0);
        result.put("feedback", "No response received from AI service");
        return result;
    }

    private Map<String, Object> extractScoreAndFeedback(String response) {
        Map<String, Object> result = new HashMap<>();
        extractScoreForFeedback(response, result);
        extractFeedback(response, result);
        log.debug("Successfully extracted score ({}) and feedback", result.get("score"));
        return result;
    }

    private Map<String, Object> handleExtractionError(Exception e) {
        log.error("Error parsing response for score and feedback: {}", e.getMessage(), e);
        Map<String, Object> result = new HashMap<>();
        result.put("score", 0.0);
        result.put("feedback", "Error evaluating answer: " + e.getMessage());
        return result;
    }

    private void extractScoreForFeedback(String response, Map<String, Object> result) {
        Pattern scorePattern = Pattern.compile("Score:\\s*(\\d+(\\.\\d+)?)");
        Matcher scoreMatcher = scorePattern.matcher(response);
        if (scoreMatcher.find()) {
            Double score = Double.parseDouble(scoreMatcher.group(1));
            Double normalizedScore = Math.min(Math.max(score, 0.0), 100.0);
            log.debug("Extracted score: {} (normalized to: {})", score, normalizedScore);
            result.put("score", normalizedScore);
        } else {
            log.warn("No score found in response, using default value");
            result.put("score", 0.0);
        }
    }

    private void extractFeedback(String response, Map<String, Object> result) {
        Pattern feedbackPattern = Pattern.compile("Feedback:\\s*(.+)", Pattern.DOTALL);
        Matcher feedbackMatcher = feedbackPattern.matcher(response);
        if (feedbackMatcher.find()) {
            String feedback = feedbackMatcher.group(1).trim();
            log.debug("Extracted feedback of length: {}", feedback.length());
            result.put("feedback", feedback);
        } else {
            log.info("No feedback section found, using entire response as feedback");
            result.put("feedback", response.trim());
        }
    }

    private Map<String, Object> extractVacancyModelFromResponse(String response) {
        log.debug("Extracting vacancy model from response");
        Map<String, Object> result = new HashMap<>();

        if (response == null || response.isEmpty()) {
            log.warn("Empty response received when extracting vacancy model");
            result.put("success", false);
            result.put("message", "Empty response");
            return result;
        }
        String cleanJson = "";

        ObjectMapper mapper = new ObjectMapper();
        try {
            String rawOutput = response.trim();
            int start = rawOutput.indexOf("{");
            int end = rawOutput.lastIndexOf("}");
            if (start >= 0 && end >= 0) {
                cleanJson = rawOutput.substring(start, end + 1);
            }

            VacancyResponse vacancyResponse = mapper.readValue(cleanJson, VacancyResponse.class);
            result.put("success", vacancyResponse != null);
            result.put("message", vacancyResponse != null ? "Successfully parsed vacancy model" : "Failed to parse vacancy model");
            result.put("data", cleanJson);
        } catch (JsonProcessingException e) {
            log.error("Error parsing vacancy response JSON: {}", cleanJson, e);
            result.put("success", false);
            result.put("message", "Error parsing JSON: " + e.getMessage());
        }
        return result;
    }


}
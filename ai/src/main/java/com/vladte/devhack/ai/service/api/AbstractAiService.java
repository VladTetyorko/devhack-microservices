package com.vladte.devhack.ai.service.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.ai.util.ResponseExtractorUtil;
import com.vladte.devhack.infra.model.arguments.KafkaPayloadArguments;
import com.vladte.devhack.infra.model.arguments.request.AnswerCheckRequestArguments;
import com.vladte.devhack.infra.model.arguments.request.QuestionGenerateRequestArguments;
import com.vladte.devhack.infra.model.arguments.request.VacancyParseFromTestRequestArguments;
import com.vladte.devhack.infra.model.payload.RequestPayload;
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
    protected final ObjectMapper objectMapper;

    // Abstract methods for configuration
    protected abstract String getApiKey();

    protected abstract String getModel();

    protected abstract int getMaxTokens();

    protected abstract String getApiUrl();

    protected AbstractAiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
        this.webClient = WebClient.builder().build();
        log.debug("Initialized AbstractAiService with default RestTemplate and WebClient");
    }

    // API Request/Response handling
    protected HttpEntity<Map<String, Object>> createApiRequest(String prompt) {
        log.debug("Creating API request for model: {}", getModel());
        HttpHeaders headers = createRequestHeaders();
        log.debug("→ Authorization header = {}", headers.getFirst(HttpHeaders.AUTHORIZATION));
        Map<String, Object> requestBody = createRequestBody(prompt);
        log.debug("→ Request Body = {}", requestBody);

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
    public CompletableFuture<String> generateQuestionsForTagAsync(RequestPayload<QuestionGenerateRequestArguments> payload) {
        log.debug("Generating questions asynchronously for tag '{}' at {} difficulty", payload.getArguments().getTag(), payload.getArguments().getDifficulty());
        String prompt = generateTextForAiRequest(payload);
        return generateTextAsync(prompt);
    }

    @Override
    @Async
    public CompletableFuture<Boolean> checkAnswerForCheatingAsync(RequestPayload<AnswerCheckRequestArguments> payload) {
        log.debug("Checking if answer contains cheating asynchronously for question: '{}'", payload.getArguments().getQuestion());
        String prompt = generateTextForAiRequest(payload);
        return generateTextAsync(prompt)
                .thenApply(ResponseExtractorUtil::extractCheatingResultFromResponse);
    }

    @Override
    @Async
    public CompletableFuture<Map<String, Object>> checkAnswerWithFeedbackAsync(RequestPayload<AnswerCheckRequestArguments> payload) {
        log.debug("Checking answer with feedback asynchronously for question: '{}'", payload.getArguments().getQuestion());
        String prompt = generateTextForAiRequest(payload);
        return generateTextAsync(prompt)
                .thenApply(ResponseExtractorUtil::extractScoreAndFeedbackFromResponse);
    }

    @Override
    @Async
    public CompletableFuture<Map<String, Object>> extractVacancyModelFromDescription(RequestPayload<VacancyParseFromTestRequestArguments> payload) {
        log.debug("Extracting vacancy model from description asynchronously for vacancy description: '{}'", payload.getArguments().getText());
        String prompt = generateTextForAiRequest(payload);
        return generateTextAsync(prompt)
                .thenApply(responseFromAi -> ResponseExtractorUtil.extractVacancyModelFromResponse(responseFromAi, objectMapper));
    }


    protected <T extends KafkaPayloadArguments> String generateTextForAiRequest(RequestPayload<T> payload) {
        return formatPromptTemplate(payload.getPrompt(), payload.getArguments().getAsList());
    }

    protected String formatPromptTemplate(String promptTemplate, List<String> arguments) {
        if (promptTemplate == null || arguments == null) {
            log.warn("Null prompt template or arguments received");
            return "";
        }
        try {
            return String.format(promptTemplate, arguments.toArray());
        } catch (Exception e) {
            log.error("Error formatting prompt template: {}", e.getMessage(), e);
            return "";
        }
    }


}
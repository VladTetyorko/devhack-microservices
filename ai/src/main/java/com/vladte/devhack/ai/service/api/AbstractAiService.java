package com.vladte.devhack.ai.service.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.ai.util.ResponseExtractorUtil;
import com.vladte.devhack.infra.model.payload.request.AiRenderedRequestPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

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

    protected abstract HttpEntity<Map<String, Object>> createApiRequestBody(String prompt);

    protected AbstractAiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
        this.webClient = WebClient.builder().build();
        log.debug("Initialized AbstractAiService with default RestTemplate and WebClient");
    }

    private void handleApiCallException(Exception e) {
        switch (e) {
            case HttpClientErrorException httpClientErrorException ->
                    log.error("Client error calling AI API: {}", httpClientErrorException.getMessage(), httpClientErrorException);
            case HttpServerErrorException httpServerErrorException ->
                    log.error("Server error from AI API: {}", httpServerErrorException.getMessage(), httpServerErrorException);
            case RestClientException restClientException ->
                    log.error("Error communicating with AI API: {}", restClientException.getMessage(), restClientException);
            default -> log.error("Unexpected error during API call: {}", e.getMessage(), e);
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
        Map<String, Object> choice = choices.getFirst();
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


    private CompletableFuture<String> sendAiRequest(String prompt) {
        log.debug("Executing text generation asynchronously with prompt length: {}", prompt.length());
        try {
            HttpEntity<Map<String, Object>> request = createApiRequestBody(prompt);
            return callApiInternally(request)
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

    private CompletableFuture<Map<String, Object>> callApiInternally(HttpEntity<Map<String, Object>> request) {
        log.debug("Calling AI API asynchronously at: {}", getApiUrl());
        return postRequest(request)
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
    private CompletableFuture<Map<String, Object>> postRequest(HttpEntity<Map<String, Object>> request) {
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


    @Override
    @Async
    public CompletableFuture<String> generateQuestionsForTagAsync(AiRenderedRequestPayload payload) {
        log.debug("Generating questions asynchronously via chat request");
        String prompt = payload.getInput() != null ? payload.getInput() : "";
        return sendAiRequest(prompt);
    }

    @Override
    @Async
    public CompletableFuture<Boolean> checkAnswerForCheatingAsync(AiRenderedRequestPayload payload) {
        log.debug("Checking if answer contains cheating asynchronously (generic JSON payload)");
        String prompt = payload.getInput() != null ? payload.getInput() : "";
        return sendAiRequest(prompt)
                .thenApply(ResponseExtractorUtil::extractCheatingResultFromResponse);
    }

    @Override
    @Async
    public CompletableFuture<Map<String, Object>> checkAnswerWithFeedbackAsync(AiRenderedRequestPayload payload) {
        log.debug("Checking answer with feedback asynchronously (generic JSON payload)");
        String prompt = payload.getInput() != null ? payload.getInput() : "";
        return sendAiRequest(prompt)
                .thenApply(ResponseExtractorUtil::extractScoreAndFeedbackFromResponse);
    }

    @Override
    @Async
    public CompletableFuture<Map<String, Object>> extractVacancyModelFromDescription(AiRenderedRequestPayload payload) {
        log.debug("Extracting vacancy model from description asynchronously (generic JSON payload)");
        String prompt = payload.getInput() != null ? payload.getInput() : "";
        return sendAiRequest(prompt)
                .thenApply(responseFromAi -> ResponseExtractorUtil.extractVacancyModelFromResponse(responseFromAi, objectMapper));
    }

}
package com.vladte.devhack.ai.service.api;

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

    /**
     * Default constructor that creates a RestTemplate and WebClient instance.
     * In a production environment, consider using dependency injection instead.
     */
    protected AbstractAiService() {
        this.restTemplate = new RestTemplate();
        this.webClient = WebClient.builder().build();
        log.debug("Initialized AbstractAiService with default RestTemplate and WebClient");
    }

    /**
     * Get the API key for the AI service.
     *
     * @return the API key
     */
    protected abstract String getApiKey();

    /**
     * Get the model name for the AI service.
     *
     * @return the model name
     */
    protected abstract String getModel();

    /**
     * Get the maximum number of tokens for the AI service.
     *
     * @return the maximum number of tokens
     */
    protected abstract int getMaxTokens();

    /**
     * Get the API URL for the AI service.
     *
     * @return the API URL
     */
    protected abstract String getApiUrl();

    /**
     * Private helper method to generate text synchronously.
     * This is used internally by the class.
     *
     * @param prompt the prompt to send to the API
     * @return the generated text
     */
    private String generateText(String prompt) {
        log.debug("Generating text with prompt length: {}", prompt.length());

        try {
            return executeTextGeneration(prompt);
        } catch (Exception e) {
            return handleTextGenerationError(e);
        }
    }

    /**
     * Executes the text generation process.
     *
     * @param prompt the prompt to send to the API
     * @return the generated text
     * @throws Exception if an error occurs during text generation
     */
    private String executeTextGeneration(String prompt) {
        // Prepare the request
        HttpEntity<Map<String, Object>> request = createApiRequest(prompt);

        // Make the API call
        Map<String, Object> responseBody = callApi(request);

        // Parse the response
        return parseApiResponse(responseBody);
    }

    /**
     * Executes the text generation process asynchronously.
     *
     * @param prompt the prompt to send to the API
     * @return a CompletableFuture containing the generated text
     */
    private CompletableFuture<String> executeTextGenerationAsync(String prompt) {
        log.debug("Executing text generation asynchronously with prompt length: {}", prompt.length());

        try {
            // Prepare the request
            HttpEntity<Map<String, Object>> request = createApiRequest(prompt);

            // Make the API call asynchronously
            return callApiAsync(request)
                    .thenApply(responseBody -> {
                        try {
                            // Parse the response
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

    /**
     * Handles errors that occur during text generation.
     *
     * @param e the exception that occurred
     * @return an error message
     */
    private String handleTextGenerationError(Exception e) {
        log.error("Error generating text: {}", e.getMessage(), e);
        return "Error generating text: " + e.getMessage();
    }

    /**
     * Creates the API request entity with appropriate headers and body.
     *
     * @param prompt the prompt to send to the API
     * @return the HttpEntity containing the request
     */
    protected HttpEntity<Map<String, Object>> createApiRequest(String prompt) {
        log.debug("Creating API request for model: {}", getModel());

        HttpHeaders headers = createRequestHeaders();
        Map<String, Object> requestBody = createRequestBody(prompt);

        return new HttpEntity<>(requestBody, headers);
    }

    /**
     * Creates the HTTP headers for the API request.
     *
     * @return the HttpHeaders object with content type and authorization
     */
    private HttpHeaders createRequestHeaders() {
        log.debug("Creating request headers with API key");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getApiKey());

        return headers;
    }

    /**
     * Creates the request body for the API request.
     *
     * @param prompt the prompt to send to the API
     * @return the request body as a Map
     */
    private Map<String, Object> createRequestBody(String prompt) {
        log.debug("Creating request body with prompt length: {}", prompt.length());

        Map<String, Object> message = createUserMessage(prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", getModel());
        requestBody.put("messages", List.of(message));

        return requestBody;
    }

    /**
     * Creates a user message for the API request.
     *
     * @param prompt the prompt content
     * @return the message as a Map
     */
    private Map<String, Object> createUserMessage(String prompt) {
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        return message;
    }

    /**
     * Makes the API call to the AI service.
     *
     * @param request the HttpEntity containing the request
     * @return the response body as a Map
     * @throws RestClientException if there's an error communicating with the API
     */
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

    /**
     * Makes the API call to the AI service asynchronously.
     *
     * @param request the HttpEntity containing the request
     * @return a CompletableFuture containing the response body as a Map
     */
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

    /**
     * Executes the API call to the AI service.
     *
     * @param request the HttpEntity containing the request
     * @return the ResponseEntity containing the response
     * @throws RestClientException if there's an error communicating with the API
     */
    private ResponseEntity<Map> executeApiCall(HttpEntity<Map<String, Object>> request) throws RestClientException {
        return restTemplate.postForEntity(getApiUrl(), request, Map.class);
    }

    /**
     * Executes the API call to the AI service asynchronously.
     *
     * @param request the HttpEntity containing the request
     * @return a CompletableFuture containing the response body as a Map
     */
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

    /**
     * Handles exceptions that occur during API calls.
     *
     * @param e the exception to handle
     */
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

    /**
     * Parses the API response to extract the generated text.
     *
     * @param responseBody the response body from the API
     * @return the generated text
     */
    protected String parseApiResponse(Map<String, Object> responseBody) {
        log.debug("Parsing API response");

        // Validate response body
        String validationError = validateResponseBody(responseBody);
        if (validationError != null) {
            return validationError;
        }

        // Extract choice from response
        Map<String, Object> choice = extractChoiceFromResponse(responseBody);
        if (choice == null) {
            return "Failed to generate text: no valid choice found";
        }

        // Extract content from choice
        return extractContentFromChoice(choice);
    }

    /**
     * Validates the response body from the API.
     *
     * @param responseBody the response body to validate
     * @return an error message if validation fails, null otherwise
     */
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

    /**
     * Extracts the first choice from the response.
     *
     * @param responseBody the response body from the API
     * @return the first choice as a Map, or null if no valid choice is found
     */
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

    /**
     * Extracts the content from a choice.
     *
     * @param choice the choice from which to extract content
     * @return the content as a String, or an error message if extraction fails
     */
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

    /**
     * Private helper method to generate interview questions based on a tag.
     *
     * @param tag        the tag to generate questions for
     * @param count      the number of questions to generate
     * @param difficulty the difficulty level of the questions
     * @return the generated questions
     */
    private String generateQuestionsForTag(String tag, int count, String difficulty) {
        log.debug("Generating {} questions for tag '{}' at {} difficulty", count, tag, difficulty);

        String prompt = createQuestionGenerationPrompt(tag, count, difficulty);
        return generateText(prompt);
    }

    /**
     * Creates a prompt for generating interview questions.
     *
     * @param tag        the tag to generate questions for
     * @param count      the number of questions to generate
     * @param difficulty the difficulty level of the questions
     * @return the formatted prompt
     */
    protected String createQuestionGenerationPrompt(String tag, int count, String difficulty) {
        return String.format(
                AiPromptConstraints.GENERATE_QUESTIONS_TEMPLATE,
                "Java",
                count, tag, difficulty, tag);
    }

    /**
     * Generate text using the AI API asynchronously.
     *
     * @param prompt the prompt to send to the API
     * @return a CompletableFuture containing the generated text
     */
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

    /**
     * Generate interview questions based on a tag asynchronously.
     *
     * @param tag        the tag to generate questions for
     * @param count      the number of questions to generate
     * @param difficulty the difficulty level of the questions
     * @return a CompletableFuture containing the generated questions
     */
    @Override
    @Async
    public CompletableFuture<String> generateQuestionsForTagAsync(String tag, int count, String difficulty) {
        log.debug("Generating questions asynchronously for tag '{}' at {} difficulty", tag, difficulty);
        String prompt = createQuestionGenerationPrompt(tag, count, difficulty);
        return generateTextAsync(prompt);
    }

    /**
     * Private helper method to check an answer to an interview question and provide a score.
     *
     * @param questionText the text of the interview question
     * @param answerText   the text of the answer to check
     * @return a score between 0 and 100 indicating how correct the answer is
     */
    private Double checkAnswer(String questionText, String answerText) {
        log.debug("Checking answer for question: '{}'", questionText);

        String prompt = createAnswerCheckPrompt(questionText, answerText);
        String response = generateText(prompt);

        return extractScoreFromResponse(response);
    }

    /**
     * Creates a prompt for checking an answer.
     *
     * @param questionText the text of the interview question
     * @param answerText   the text of the answer to check
     * @return the formatted prompt
     */
    protected String createAnswerCheckPrompt(String questionText, String answerText) {
        return String.format(
                AiPromptConstraints.CHECK_ANSWER_TEMPLATE,
                questionText, answerText);
    }

    /**
     * Extracts a numeric score from the AI response.
     *
     * @param response the response from the AI
     * @return a score between 0 and 100
     */
    protected Double extractScoreFromResponse(String response) {
        log.debug("Extracting score from response");

        if (response == null || response.isEmpty()) {
            log.warn("Empty response received when extracting score");
            return 0.0;
        }

        try {
            Double score = findScoreInResponse(response);
            if (score != null) {
                return normalizeScore(score);
            } else {
                log.warn("No numeric score found in response: '{}'", response);
            }
        } catch (Exception e) {
            log.error("Error parsing score from response: {}", e.getMessage(), e);
        }

        // Default value if no valid score was found
        return 0.0;
    }

    /**
     * Finds a numeric score in the response string.
     *
     * @param response the response string to search
     * @return the found score as a Double, or null if no score is found
     */
    private Double findScoreInResponse(String response) {
        Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return Double.parseDouble(matcher.group());
        }

        return null;
    }

    /**
     * Normalizes a score to ensure it's within the valid range of 0-100.
     *
     * @param score the score to normalize
     * @return the normalized score
     */
    private Double normalizeScore(Double score) {
        Double normalizedScore = Math.min(Math.max(score, 0.0), 100.0);
        log.debug("Normalized score: {} to {}", score, normalizedScore);
        return normalizedScore;
    }


    /**
     * Private helper method to check an answer to an interview question and provide a score and feedback.
     *
     * @param questionText the text of the interview question
     * @param answerText   the text of the answer to check
     * @return a map containing the score and feedback
     */
    private Map<String, Object> checkAnswerWithFeedback(String questionText, String answerText) {
        log.debug("Checking answer with feedback for question: '{}'", questionText);

        String prompt = createAnswerCheckWithFeedbackPrompt(questionText, answerText);
        String response = generateText(prompt);

        return extractScoreAndFeedbackFromResponse(response);
    }

    /**
     * Creates a prompt for checking an answer with feedback.
     *
     * @param questionText the text of the interview question
     * @param answerText   the text of the answer to check
     * @return the formatted prompt
     */
    protected String createAnswerCheckWithFeedbackPrompt(String questionText, String answerText) {
        return String.format(
                AiPromptConstraints.CHECK_ANSWER_WITH_FEEDBACK_TEMPLATE,
                questionText, answerText);
    }

    /**
     * Extracts a score and feedback from the AI response.
     *
     * @param response the response from the AI
     * @return a map containing the score and feedback
     */
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

    /**
     * Creates a result map for an empty response.
     *
     * @return a map with default values for score and feedback
     */
    private Map<String, Object> createEmptyResponseResult() {
        log.warn("Empty response received when extracting score and feedback");
        Map<String, Object> result = new HashMap<>();
        result.put("score", 0.0);
        result.put("feedback", "No response received from AI service");
        return result;
    }

    /**
     * Extracts score and feedback from a valid response.
     *
     * @param response the response to extract from
     * @return a map containing the extracted score and feedback
     */
    private Map<String, Object> extractScoreAndFeedback(String response) {
        Map<String, Object> result = new HashMap<>();

        // Extract score
        extractScoreForFeedback(response, result);

        // Extract feedback
        extractFeedback(response, result);

        log.debug("Successfully extracted score ({}) and feedback", result.get("score"));
        return result;
    }

    /**
     * Handles errors that occur during score and feedback extraction.
     *
     * @param e the exception that occurred
     * @return a map with error information
     */
    private Map<String, Object> handleExtractionError(Exception e) {
        log.error("Error parsing response for score and feedback: {}", e.getMessage(), e);
        Map<String, Object> result = new HashMap<>();
        result.put("score", 0.0);
        result.put("feedback", "Error evaluating answer: " + e.getMessage());
        return result;
    }

    /**
     * Extracts the score from the response and adds it to the result map.
     *
     * @param response the response from the AI
     * @param result   the map to add the score to
     */
    private void extractScoreForFeedback(String response, Map<String, Object> result) {
        Pattern scorePattern = Pattern.compile("Score:\\s*(\\d+(\\.\\d+)?)");
        Matcher scoreMatcher = scorePattern.matcher(response);

        if (scoreMatcher.find()) {
            Double score = Double.parseDouble(scoreMatcher.group(1));
            // Ensure the score is within the valid range
            Double normalizedScore = Math.min(Math.max(score, 0.0), 100.0);
            log.debug("Extracted score: {} (normalized to: {})", score, normalizedScore);
            result.put("score", normalizedScore);
        } else {
            log.warn("No score found in response, using default value");
            result.put("score", 0.0);
        }
    }

    /**
     * Extracts the feedback from the response and adds it to the result map.
     *
     * @param response the response from the AI
     * @param result   the map to add the feedback to
     */
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

    /**
     * Check an answer to an interview question and provide a score and feedback asynchronously.
     *
     * @param questionText the text of the interview question
     * @param answerText   the text of the answer to check
     * @return a CompletableFuture containing a map with the score and feedback
     */
    @Override
    @Async
    public CompletableFuture<Map<String, Object>> checkAnswerWithFeedbackAsync(String questionText, String answerText) {
        log.debug("Checking answer with feedback asynchronously for question: '{}'", questionText);

        String prompt = createAnswerCheckWithFeedbackPrompt(questionText, answerText);
        return generateTextAsync(prompt)
                .thenApply(this::extractScoreAndFeedbackFromResponse);
    }

    /**
     * Private helper method to check if an answer to an interview question contains evidence of cheating.
     *
     * @param questionText the text of the interview question
     * @param answerText   the text of the answer to check
     * @return true if the answer contains evidence of cheating, false otherwise
     */
    private Boolean checkAnswerForCheating(String questionText, String answerText) {
        log.debug("Checking if answer contains cheating for question: '{}'", questionText);

        String prompt = createAnswerCheckForCheatingPrompt(questionText, answerText);
        String response = generateText(prompt);

        return extractCheatingResultFromResponse(response);
    }

    /**
     * Check if an answer to an interview question contains evidence of cheating asynchronously.
     *
     * @param questionText the text of the interview question
     * @param answerText   the text of the answer to check
     * @return a CompletableFuture containing true if the answer contains evidence of cheating, false otherwise
     */
    @Override
    @Async
    public CompletableFuture<Boolean> checkAnswerForCheatingAsync(String questionText, String answerText) {
        log.debug("Checking if answer contains cheating asynchronously for question: '{}'", questionText);

        String prompt = createAnswerCheckForCheatingPrompt(questionText, answerText);
        return generateTextAsync(prompt)
                .thenApply(this::extractCheatingResultFromResponse);
    }

    /**
     * Creates a prompt for checking if an answer contains cheating.
     *
     * @param questionText the text of the interview question
     * @param answerText   the text of the answer to check
     * @return the formatted prompt
     */
    protected String createAnswerCheckForCheatingPrompt(String questionText, String answerText) {
        return String.format(
                AiPromptConstraints.CHECK_ANSWER_FOR_CHEATING_TEMPLATE,
                questionText, answerText);
    }

    /**
     * Extracts a boolean result from the AI response indicating if cheating was detected.
     *
     * @param response the response from the AI
     * @return true if the answer contains evidence of cheating, false otherwise
     */
    protected Boolean extractCheatingResultFromResponse(String response) {
        log.debug("Extracting cheating result from response");

        if (response == null || response.isEmpty()) {
            log.warn("Empty response received when extracting cheating result");
            return false;
        }

        try {
            // Trim the response and convert to lowercase for case-insensitive comparison
            String trimmedResponse = response.trim().toLowerCase();

            // Check if the response is "true"
            return "true".equals(trimmedResponse);
        } catch (Exception e) {
            log.error("Error extracting cheating result from response: {}", e.getMessage(), e);
            return false;
        }
    }
}

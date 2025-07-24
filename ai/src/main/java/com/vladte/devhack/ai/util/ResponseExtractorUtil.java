package com.vladte.devhack.ai.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.entities.global.Vacancy;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ResponseExtractorUtil {
    public static Boolean extractCheatingResultFromResponse(String response) {
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

    public static Map<String, Object> extractScoreAndFeedbackFromResponse(String response) {
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

    private static Map<String, Object> createEmptyResponseResult() {
        log.warn("Empty response received when extracting score and feedback");
        Map<String, Object> result = new HashMap<>();
        result.put("score", 0.0);
        result.put("feedback", "No response received from AI service");
        return result;
    }

    private static Map<String, Object> extractScoreAndFeedback(String response) {
        Map<String, Object> result = new HashMap<>();
        extractScoreForFeedback(response, result);
        extractFeedback(response, result);
        log.debug("Successfully extracted score ({}) and feedback", result.get("score"));
        return result;
    }

    private static Map<String, Object> handleExtractionError(Exception e) {
        log.error("Error parsing response for score and feedback: {}", e.getMessage(), e);
        Map<String, Object> result = new HashMap<>();
        result.put("score", 0.0);
        result.put("feedback", "Error evaluating answer: " + e.getMessage());
        return result;
    }

    private static void extractScoreForFeedback(String response, Map<String, Object> result) {
        Pattern scorePattern = Pattern.compile("Score:\\s*(\\d+(\\.\\d+)?)");
        Matcher scoreMatcher = scorePattern.matcher(response);
        if (scoreMatcher.find()) {
            double score = Double.parseDouble(scoreMatcher.group(1));
            Double normalizedScore = Math.min(Math.max(score, 0.0), 100.0);
            log.debug("Extracted score: {} (normalized to: {})", score, normalizedScore);
            result.put("score", normalizedScore);
        } else {
            log.warn("No score found in response, using default value");
            result.put("score", 0.0);
        }
    }

    private static void extractFeedback(String response, Map<String, Object> result) {
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

    public static Map<String, Object> extractVacancyModelFromResponse(String response, ObjectMapper objectMapper) {
        log.debug("Extracting vacancy model from response");
        Map<String, Object> result = new HashMap<>();

        if (response == null || response.isEmpty()) {
            log.warn("Empty response received when extracting vacancy model");
            result.put("success", false);
            result.put("message", "Empty response");
            return result;
        }
        String cleanJson = "";

        try {
            String rawOutput = response.trim();
            int start = rawOutput.indexOf("{");
            int end = rawOutput.lastIndexOf("}");
            if (start >= 0 && end >= 0) {
                cleanJson = rawOutput.substring(start, end + 1);
            }

            Vacancy vacancy = objectMapper.readValue(cleanJson, Vacancy.class);
            result.put("success", vacancy != null);
            result.put("message", vacancy != null ? "Successfully parsed vacancy model" : "Failed to parse vacancy model");
            result.put("data", cleanJson);
        } catch (JsonProcessingException e) {
            log.error("Error parsing vacancy response JSON: {}", cleanJson, e);
            result.put("success", false);
            result.put("message", "Error parsing JSON: " + e.getMessage());
        }
        return result;
    }
}

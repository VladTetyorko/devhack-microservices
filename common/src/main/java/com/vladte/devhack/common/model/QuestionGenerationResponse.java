package com.vladte.devhack.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Model class for question generation responses.
 * This class is used to parse the response from the AI module after generating questions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionGenerationResponse implements Serializable {
    private String tagName;
    private String difficulty;
    private List<String> questionTexts;
    private String status;
    private String errorMessage;

    /**
     * Factory method to create a successful response.
     *
     * @param tagName       the name of the tag the questions were generated for
     * @param difficulty    the difficulty level of the questions
     * @param questionTexts the list of generated question texts
     * @return a new QuestionGenerationResponse instance
     */
    public static QuestionGenerationResponse success(String tagName, String difficulty, List<String> questionTexts) {
        return new QuestionGenerationResponse(tagName, difficulty, questionTexts, "success", null);
    }

    /**
     * Factory method to create an error response.
     *
     * @param tagName      the name of the tag the questions were supposed to be generated for
     * @param difficulty   the difficulty level of the questions
     * @param errorMessage the error message
     * @return a new QuestionGenerationResponse instance
     */
    public static QuestionGenerationResponse error(String tagName, String difficulty, String errorMessage) {
        return new QuestionGenerationResponse(tagName, difficulty, null, "error", errorMessage);
    }
}
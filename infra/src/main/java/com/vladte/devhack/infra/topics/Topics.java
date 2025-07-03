package com.vladte.devhack.infra.topics;

/**
 * Centralized definition of Kafka topics used for communication between modules.
 * This class contains constants for all topics used in the application.
 */
public class Topics {


    // New specific topic names for question generation
    public static final String QUESTION_GENERATE_REQUEST = "question.generate.request";
    public static final String QUESTION_GENERATE_RESULT = "question.generate.result";

    // New specific topic names for answer feedback
    public static final String ANSWER_FEEDBACK_REQUEST = "answer.feedback.request";
    public static final String ANSWER_FEEDBACK_RESULT = "answer.feedback.result";


    public static final String VACANCY_PARSING_REQUEST = "vacancy.parsing.request";
    public static final String VACANCY_PARSING_RESULT = "vacancy.parsing.result";

    private Topics() {
        // Private constructor to prevent instantiation
    }
}

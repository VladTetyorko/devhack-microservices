package com.vladte.devhack.infra.message;

import lombok.Getter;

/**
 * Centralized definition of Kafka message types used for communication between modules.
 * This class contains constants for all message types used in the application.
 */
@Getter
public enum MessageTypes {

    // Answer feedback message types
    CHECK_ANSWER_FOR_CHEATING("check-answer-for-cheating"),
    CHECK_ANSWER_WITH_FEEDBACK("check-answer-with-feedback"),
    CHECK_ANSWER_RESULT("check-answer-result"),

    // Question generation message types
    QUESTION_GENERATE("question-generate"),
    QUESTION_GENERATE_RESULT("question-generate-result"),

    VACANCY_PARSING ("vacancy-parsing"),
    VACANCY_PARSING_RESULT ("vacancy-parsing-result");

    private final String value;

    MessageTypes(String value) {
        this.value = value;
    }

    public static MessageTypes fromValue(String value) {
        for (MessageTypes type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown MessageType value: " + value);
    }


}

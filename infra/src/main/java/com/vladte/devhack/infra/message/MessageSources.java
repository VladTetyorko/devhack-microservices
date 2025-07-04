package com.vladte.devhack.infra.message;

/**
 * Centralized definition of Kafka message sources used for communication between modules.
 * This class contains constants for all message sources used in the application.
 */
public class MessageSources {

    // Module sources
    public static final String AI_APP = "ai-app";
    public static final String MAIN_APP = "main-app";

    MessageSources() {
    }
}
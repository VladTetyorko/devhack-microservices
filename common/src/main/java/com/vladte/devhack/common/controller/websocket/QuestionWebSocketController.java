package com.vladte.devhack.common.controller.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket controller for handling interview question subscriptions and messages.
 */
@Controller
@Slf4j
public class QuestionWebSocketController {

    /**
     * Handle subscription to question updates.
     * This method is called when a client subscribes to /topic/questions
     */
    @SubscribeMapping("/topic/questions")
    public Map<String, Object> handleQuestionSubscription() {
        log.debug("Client subscribed to question updates");

        Map<String, Object> response = new HashMap<>();
        response.put("type", "SUBSCRIPTION_CONFIRMED");
        response.put("message", "Successfully subscribed to question updates");
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }

    /**
     * Handle client ping messages to keep connection alive.
     */
    @MessageMapping("/questions/ping")
    @SendTo("/topic/questions/pong")
    public Map<String, Object> handlePing() {
        log.debug("Received ping from client");

        Map<String, Object> response = new HashMap<>();
        response.put("type", "PONG");
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }

    /**
     * Handle client requests for current question list refresh.
     * This can be used to trigger a refresh without waiting for updates.
     */
    @MessageMapping("/questions/refresh")
    @SendTo("/topic/questions")
    public Map<String, Object> handleRefreshRequest() {
        log.debug("Received refresh request from client");

        Map<String, Object> response = new HashMap<>();
        response.put("type", "REFRESH_REQUESTED");
        response.put("message", "Question list refresh requested");
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }
}
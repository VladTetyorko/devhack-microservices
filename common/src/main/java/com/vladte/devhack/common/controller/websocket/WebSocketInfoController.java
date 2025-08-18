package com.vladte.devhack.common.controller.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for WebSocket information endpoints.
 * Provides information about WebSocket server capabilities and configuration.
 */
@RestController
@RequestMapping("/ws")
@CrossOrigin(origins = {"http://localhost:4200", "https://devhack.com"})
@Slf4j
public class WebSocketInfoController {

    /**
     * Get WebSocket server information.
     * This endpoint is typically used by SockJS clients to discover server capabilities.
     *
     * @param timestamp timestamp parameter (optional, used for cache busting)
     * @return WebSocket server information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getWebSocketInfo(
            @RequestParam(value = "t", required = false) Long timestamp) {
        log.debug("WebSocket info requested with timestamp: {}", timestamp);

        Map<String, Object> info = new HashMap<>();

        // WebSocket server capabilities
        info.put("websocket", true);
        info.put("origins", List.of("*:*"));
        info.put("cookie_needed", false);
        info.put("entropy", generateEntropy());

        // Transport options
        Map<String, Object> transports = new HashMap<>();
        transports.put("websocket", true);
        transports.put("xhr-streaming", true);
        transports.put("xhr-polling", true);
        transports.put("jsonp-polling", true);
        info.put("transports", transports);

        // Server information
        info.put("server_heartbeat_interval", 25000);
        info.put("heartbeat_timeout", 60000);
        info.put("disconnect_delay", 5000);

        // Additional metadata
        info.put("timestamp", System.currentTimeMillis());
        info.put("version", "1.0.0");

        log.debug("Returning WebSocket info: {}", info);
        return ResponseEntity.ok(info);
    }

    /**
     * Get WebSocket connection status.
     *
     * @return current WebSocket server status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getWebSocketStatus() {
        log.debug("WebSocket status requested");

        Map<String, Object> status = new HashMap<>();
        status.put("status", "active");
        status.put("uptime", System.currentTimeMillis());
        status.put("connections", 0); // This could be enhanced to track actual connections
        status.put("topics", List.of("/topic/questions", "/topic/questions/pong"));
        status.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(status);
    }

    /**
     * Health check endpoint for WebSocket service.
     *
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getWebSocketHealth() {
        log.debug("WebSocket health check requested");

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "WebSocket");
        health.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(health);
    }

    /**
     * Generate entropy value for SockJS.
     * This is used by SockJS for connection randomization.
     *
     * @return random entropy value
     */
    private long generateEntropy() {
        return (long) (Math.random() * Integer.MAX_VALUE);
    }
}
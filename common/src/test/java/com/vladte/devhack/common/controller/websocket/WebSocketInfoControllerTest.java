package com.vladte.devhack.common.controller.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for WebSocketInfoController.
 * Tests WebSocket information endpoints.
 */
@DisplayName("WebSocket Info Controller Tests")
class WebSocketInfoControllerTest {

    private WebSocketInfoController controller;

    @BeforeEach
    void setUp() {
        controller = new WebSocketInfoController();
    }

    @Test
    @DisplayName("Should return WebSocket info with timestamp parameter")
    void testGetWebSocketInfoWithTimestamp() {
        // Given
        Long timestamp = 1754906392385L;

        // When
        ResponseEntity<Map<String, Object>> response = controller.getWebSocketInfo(timestamp);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        // Verify WebSocket capabilities
        assertTrue((Boolean) body.get("websocket"));
        assertEquals(List.of("*:*"), body.get("origins"));
        assertFalse((Boolean) body.get("cookie_needed"));
        assertNotNull(body.get("entropy"));

        // Verify transports
        @SuppressWarnings("unchecked")
        Map<String, Object> transports = (Map<String, Object>) body.get("transports");
        assertNotNull(transports);
        assertTrue((Boolean) transports.get("websocket"));
        assertTrue((Boolean) transports.get("xhr-streaming"));
        assertTrue((Boolean) transports.get("xhr-polling"));
        assertTrue((Boolean) transports.get("jsonp-polling"));

        // Verify server information
        assertEquals(25000, body.get("server_heartbeat_interval"));
        assertEquals(60000, body.get("heartbeat_timeout"));
        assertEquals(5000, body.get("disconnect_delay"));

        // Verify metadata
        assertNotNull(body.get("timestamp"));
        assertEquals("1.0.0", body.get("version"));
    }

    @Test
    @DisplayName("Should return WebSocket info without timestamp parameter")
    void testGetWebSocketInfoWithoutTimestamp() {
        // When
        ResponseEntity<Map<String, Object>> response = controller.getWebSocketInfo(null);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        // Verify essential fields are present
        assertTrue(body.containsKey("websocket"));
        assertTrue(body.containsKey("origins"));
        assertTrue(body.containsKey("transports"));
        assertTrue(body.containsKey("timestamp"));
    }

    @Test
    @DisplayName("Should return WebSocket status")
    void testGetWebSocketStatus() {
        // When
        ResponseEntity<Map<String, Object>> response = controller.getWebSocketStatus();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals("active", body.get("status"));
        assertNotNull(body.get("uptime"));
        assertEquals(0, body.get("connections"));
        assertEquals(List.of("/topic/questions", "/topic/questions/pong"), body.get("topics"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    @DisplayName("Should return WebSocket health status")
    void testGetWebSocketHealth() {
        // When
        ResponseEntity<Map<String, Object>> response = controller.getWebSocketHealth();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals("UP", body.get("status"));
        assertEquals("WebSocket", body.get("service"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    @DisplayName("Should generate different entropy values")
    void testEntropyGeneration() {
        // When
        ResponseEntity<Map<String, Object>> response1 = controller.getWebSocketInfo(null);
        ResponseEntity<Map<String, Object>> response2 = controller.getWebSocketInfo(null);

        // Then
        Map<String, Object> body1 = response1.getBody();
        Map<String, Object> body2 = response2.getBody();

        assertNotNull(body1);
        assertNotNull(body2);

        Long entropy1 = (Long) body1.get("entropy");
        Long entropy2 = (Long) body2.get("entropy");

        assertNotNull(entropy1);
        assertNotNull(entropy2);
        // Entropy values should be different (with very high probability)
        assertNotEquals(entropy1, entropy2);
    }
}
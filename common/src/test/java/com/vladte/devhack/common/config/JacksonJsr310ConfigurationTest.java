package com.vladte.devhack.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test to verify that Jackson ObjectMapper with JSR310 module
 * can properly serialize and deserialize LocalDateTime objects.
 * This test addresses the original serialization issue without requiring Redis connectivity.
 */
public class JacksonJsr310ConfigurationTest {

    @Test
    public void testObjectMapperWithJsr310CanSerializeLocalDateTime() throws Exception {
        System.out.println("[DEBUG_LOG] Testing ObjectMapper with JSR310 module for LocalDateTime serialization");

        // Create ObjectMapper with JSR310 module (same as in CacheConfig)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Create a test object with LocalDateTime
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> testData = new HashMap<>();
        testData.put("id", "test-id");
        testData.put("createdAt", now);
        testData.put("name", "Test User");

        // Test serialization
        String json = objectMapper.writeValueAsString(testData);
        assertNotNull(json, "JSON serialization should not be null");
        assertTrue(json.contains("createdAt"), "JSON should contain createdAt field");

        System.out.println("[DEBUG_LOG] Serialized JSON: " + json);

        // Test deserialization
        @SuppressWarnings("unchecked")
        Map<String, Object> deserializedData = objectMapper.readValue(json, Map.class);
        assertNotNull(deserializedData, "Deserialized data should not be null");
        assertTrue(deserializedData.containsKey("createdAt"), "Deserialized data should contain createdAt");

        System.out.println("[DEBUG_LOG] LocalDateTime serialization/deserialization test passed");
    }

    @Test
    public void testGenericJackson2JsonRedisSerializerWithJsr310() throws Exception {
        System.out.println("[DEBUG_LOG] Testing GenericJackson2JsonRedisSerializer with JSR310 module");

        // Create ObjectMapper with JSR310 module (same as in CacheConfig)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Create GenericJackson2JsonRedisSerializer with custom ObjectMapper
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Create a test object with LocalDateTime
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> testData = new HashMap<>();
        testData.put("id", "test-id");
        testData.put("createdAt", now);
        testData.put("name", "Test User");

        // Test serialization
        byte[] serializedData = serializer.serialize(testData);
        assertNotNull(serializedData, "Serialized data should not be null");
        assertTrue(serializedData.length > 0, "Serialized data should not be empty");

        // Test deserialization
        Object deserializedData = serializer.deserialize(serializedData);
        assertNotNull(deserializedData, "Deserialized data should not be null");

        System.out.println("[DEBUG_LOG] GenericJackson2JsonRedisSerializer with JSR310 test passed");
    }

    @Test
    public void testJavaTimeModuleIsRegistered() {
        System.out.println("[DEBUG_LOG] Testing that JavaTimeModule is properly registered");

        // Create ObjectMapper with JSR310 module
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Verify that the module is registered
        assertTrue(objectMapper.getRegisteredModuleIds().contains("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule"),
                "JavaTimeModule should be registered");

        System.out.println("[DEBUG_LOG] JavaTimeModule registration test passed");
    }

    @Test
    public void testLocalDateTimeSerializationFormat() throws Exception {
        System.out.println("[DEBUG_LOG] Testing LocalDateTime serialization format");

        // Create ObjectMapper with JSR310 module
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Create a specific LocalDateTime for testing
        LocalDateTime testDateTime = LocalDateTime.of(2023, 12, 25, 15, 30, 45);

        // Serialize LocalDateTime
        String json = objectMapper.writeValueAsString(testDateTime);
        assertNotNull(json, "Serialized LocalDateTime should not be null");

        // Deserialize LocalDateTime
        LocalDateTime deserializedDateTime = objectMapper.readValue(json, LocalDateTime.class);
        assertEquals(testDateTime, deserializedDateTime, "Deserialized LocalDateTime should match original");

        System.out.println("[DEBUG_LOG] LocalDateTime format: " + json);
        System.out.println("[DEBUG_LOG] LocalDateTime serialization format test passed");
    }
}
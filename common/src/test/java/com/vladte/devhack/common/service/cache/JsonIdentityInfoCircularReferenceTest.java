package com.vladte.devhack.common.service.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vladte.devhack.entities.enums.AuthProviderType;
import com.vladte.devhack.entities.user.AuthenticationProvider;
import com.vladte.devhack.entities.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test to verify that @JsonIdentityInfo annotations on User and AuthenticationProvider entities
 * successfully prevent StackOverflowError during JSON serialization of circular references.
 * This test does not require Spring context and focuses purely on Jackson serialization behavior.
 */
public class JsonIdentityInfoCircularReferenceTest {

    @Test
    public void testJsonIdentityInfoPreventsStackOverflowError() {
        try {

            // Create a test User with AuthenticationProvider (simulating the circular reference)
            User testUser = new User();
            testUser.setId(UUID.randomUUID());
            testUser.setCreatedAt(LocalDateTime.now());
            testUser.setUpdatedAt(LocalDateTime.now());
            testUser.setAuthProviders(new ArrayList<>());

            // Create AuthenticationProvider that references back to User
            AuthenticationProvider authProvider = new AuthenticationProvider();
            authProvider.setId(UUID.randomUUID());
            authProvider.setCreatedAt(LocalDateTime.now());
            authProvider.setUpdatedAt(LocalDateTime.now());
            authProvider.setUser(testUser);
            authProvider.setProvider(AuthProviderType.LOCAL);
            authProvider.setEmail("test@example.com");
            authProvider.setPasswordHash("hashedPassword");

            // Add the auth provider to user (creating circular reference)
            testUser.getAuthProviders().add(authProvider);

            // Create ObjectMapper with same configuration as CacheConfig
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule())
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

            // Test direct ObjectMapper serialization
            String jsonString = objectMapper.writeValueAsString(testUser);
            assertNotNull(jsonString, "JSON string should not be null");
            assertTrue(jsonString.length() > 0, "JSON string should not be empty");
            assertTrue(jsonString.contains("@id"), "JSON should contain @id references for identity handling");

            // Test deserialization
            User deserializedUser = objectMapper.readValue(jsonString, User.class);
            assertNotNull(deserializedUser, "Deserialized user should not be null");
            assertEquals(testUser.getId(), deserializedUser.getId(), "User IDs should match");
            assertNotNull(deserializedUser.getAuthProviders(), "AuthProviders should not be null");
            assertFalse(deserializedUser.getAuthProviders().isEmpty(), "AuthProviders should not be empty");

            AuthenticationProvider deserializedAuthProvider = deserializedUser.getAuthProviders().get(0);
            assertEquals(authProvider.getId(), deserializedAuthProvider.getId(), "AuthProvider IDs should match");
            assertEquals(authProvider.getEmail(), deserializedAuthProvider.getEmail(), "AuthProvider emails should match");
            assertNotNull(deserializedAuthProvider.getUser(), "AuthProvider user reference should not be null");
            assertEquals(testUser.getId(), deserializedAuthProvider.getUser().getId(), "Circular reference should be preserved");

        } catch (StackOverflowError e) {
            fail("@JsonIdentityInfo should prevent StackOverflowError: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail("JSON serialization should work without errors: " + e.getMessage());
        }
    }

    @Test
    public void testGenericJackson2JsonRedisSerializerWithCircularReference() {
        try {
            // Create a test User with AuthenticationProvider (simulating the circular reference)
            User testUser = new User();
            testUser.setId(UUID.randomUUID());
            testUser.setCreatedAt(LocalDateTime.now());
            testUser.setUpdatedAt(LocalDateTime.now());
            testUser.setAuthProviders(new ArrayList<>());

            // Create AuthenticationProvider that references back to User
            AuthenticationProvider authProvider = new AuthenticationProvider();
            authProvider.setId(UUID.randomUUID());
            authProvider.setCreatedAt(LocalDateTime.now());
            authProvider.setUpdatedAt(LocalDateTime.now());
            authProvider.setUser(testUser);
            authProvider.setProvider(AuthProviderType.LOCAL);
            authProvider.setEmail("test@example.com");
            authProvider.setPasswordHash("hashedPassword");

            // Add the auth provider to user (creating circular reference)
            testUser.getAuthProviders().add(authProvider);

            // Create ObjectMapper with same configuration as CacheConfig
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule())
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

            // Create GenericJackson2JsonRedisSerializer with custom ObjectMapper (same as CacheConfig)
            GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

            // Test serialization (this would cause StackOverflowError without @JsonIdentityInfo)
            byte[] serializedData = serializer.serialize(testUser);
            assertNotNull(serializedData, "Serialized data should not be null");
            assertTrue(serializedData.length > 0, "Serialized data should not be empty");

            // Test deserialization
            Object deserializedData = serializer.deserialize(serializedData);
            assertNotNull(deserializedData, "Deserialized data should not be null");
            assertInstanceOf(User.class, deserializedData, "Deserialized data should be User instance");

            User deserializedUser = (User) deserializedData;
            assertEquals(testUser.getId(), deserializedUser.getId(), "User IDs should match");
            assertNotNull(deserializedUser.getAuthProviders(), "AuthProviders should not be null");
            assertFalse(deserializedUser.getAuthProviders().isEmpty(), "AuthProviders should not be empty");

            AuthenticationProvider deserializedAuthProvider = deserializedUser.getAuthProviders().get(0);
            assertEquals(authProvider.getId(), deserializedAuthProvider.getId(), "AuthProvider IDs should match");
            assertEquals(authProvider.getEmail(), deserializedAuthProvider.getEmail(), "AuthProvider emails should match");
            assertNotNull(deserializedAuthProvider.getUser(), "AuthProvider user reference should not be null");
            assertEquals(testUser.getId(), deserializedAuthProvider.getUser().getId(), "Circular reference should be preserved");

        } catch (StackOverflowError e) {
            fail("@JsonIdentityInfo should prevent StackOverflowError in Redis serializer: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Redis serialization should work without errors: " + e.getMessage());
        }
    }

    @Test
    public void testAnnotationsArePresent() {

        // Verify that User class has @JsonIdentityInfo annotation
        assertTrue(User.class.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonIdentityInfo.class),
                "User class should have @JsonIdentityInfo annotation");

        // Verify that AuthenticationProvider class has @JsonIdentityInfo annotation
        assertTrue(AuthenticationProvider.class.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonIdentityInfo.class),
                "AuthenticationProvider class should have @JsonIdentityInfo annotation");

        // Get the annotation details
        var userAnnotation = User.class.getAnnotation(com.fasterxml.jackson.annotation.JsonIdentityInfo.class);
        var authProviderAnnotation = AuthenticationProvider.class.getAnnotation(com.fasterxml.jackson.annotation.JsonIdentityInfo.class);

        assertEquals("id", userAnnotation.property(), "User @JsonIdentityInfo should use 'id' property");
        assertEquals("id", authProviderAnnotation.property(), "AuthenticationProvider @JsonIdentityInfo should use 'id' property");
    }

    @Test
    public void testMultipleCircularReferences() {
        try {

            // Create a user with multiple auth providers
            User testUser = new User();
            testUser.setId(UUID.randomUUID());
            testUser.setCreatedAt(LocalDateTime.now());
            testUser.setUpdatedAt(LocalDateTime.now());
            testUser.setAuthProviders(new ArrayList<>());

            // Create multiple AuthenticationProviders that reference back to User
            for (int i = 0; i < 3; i++) {
                AuthenticationProvider authProvider = new AuthenticationProvider();
                authProvider.setId(UUID.randomUUID());
                authProvider.setCreatedAt(LocalDateTime.now());
                authProvider.setUpdatedAt(LocalDateTime.now());
                authProvider.setUser(testUser);
                authProvider.setProvider(AuthProviderType.LOCAL);
                authProvider.setEmail("test" + i + "@example.com");
                authProvider.setPasswordHash("hashedPassword" + i);

                testUser.getAuthProviders().add(authProvider);
            }

            // Create ObjectMapper with same configuration as CacheConfig
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule())
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

            // Test serialization with multiple circular references
            String jsonString = objectMapper.writeValueAsString(testUser);
            assertNotNull(jsonString, "JSON string should not be null");
            assertTrue(jsonString.length() > 0, "JSON string should not be empty");

            // Test deserialization
            User deserializedUser = objectMapper.readValue(jsonString, User.class);
            assertNotNull(deserializedUser, "Deserialized user should not be null");
            assertEquals(testUser.getId(), deserializedUser.getId(), "User IDs should match");
            assertEquals(3, deserializedUser.getAuthProviders().size(), "Should have 3 auth providers");

            // Verify all circular references are preserved
            for (int i = 0; i < 3; i++) {
                AuthenticationProvider deserializedAuthProvider = deserializedUser.getAuthProviders().get(i);
                assertNotNull(deserializedAuthProvider.getUser(), "AuthProvider user reference should not be null");
                assertEquals(testUser.getId(), deserializedAuthProvider.getUser().getId(), "Circular reference should be preserved");
            }


        } catch (StackOverflowError e) {
            fail("@JsonIdentityInfo should handle multiple circular references: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Multiple circular references should work without errors: " + e.getMessage());
        }
    }
}
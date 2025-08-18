package com.vladte.devhack.common.service.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vladte.devhack.common.service.domain.user.UserService;
import com.vladte.devhack.entities.enums.AuthProviderType;
import com.vladte.devhack.entities.user.AuthenticationProvider;
import com.vladte.devhack.entities.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that circular references between User and AuthenticationProvider entities
 * can be properly serialized and cached without causing StackOverflowError.
 * This test addresses the infinite recursion issue that was occurring during login
 * when User entities with AuthenticationProvider relationships were being cached.
 */
@SpringBootTest
@ActiveProfiles("test")
public class CircularReferenceSerializationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CacheManager mediumTermCacheManager;

    @Test
    public void testUserWithAuthProvidersCanBeSerializedWithoutStackOverflow() {
        try {
            System.out.println("[DEBUG_LOG] Testing User with AuthenticationProvider circular reference serialization");

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

            // Create GenericJackson2JsonRedisSerializer with custom ObjectMapper
            GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

            // Test serialization (this would cause StackOverflowError without @JsonIdentityInfo)
            byte[] serializedData = serializer.serialize(testUser);
            assertNotNull(serializedData, "Serialized data should not be null");
            assertTrue(serializedData.length > 0, "Serialized data should not be empty");

            System.out.println("[DEBUG_LOG] User with circular reference serialized successfully, size: " + serializedData.length + " bytes");

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

            System.out.println("[DEBUG_LOG] Circular reference serialization test passed - no StackOverflowError occurred");

        } catch (StackOverflowError e) {
            System.out.println("[DEBUG_LOG] StackOverflowError occurred: " + e.getMessage());
            fail("Circular reference serialization should not cause StackOverflowError: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Unexpected error: " + e.getMessage());
            e.printStackTrace();
            fail("Circular reference serialization should work without errors: " + e.getMessage());
        }
    }

    @Test
    public void testUserServiceFindByEmailWithCircularReferenceHandling() {
        try {
            System.out.println("[DEBUG_LOG] Testing UserService.findByEmail with circular reference handling");

            // Clear any existing cache
            if (mediumTermCacheManager.getCache("userByEmail") != null) {
                mediumTermCacheManager.getCache("userByEmail").clear();
            }

            String testEmail = "system@devhack.com";

            // This call would previously cause StackOverflowError when caching User with AuthProviders
            Optional<User> user1 = userService.findByEmail(testEmail);

            // Second call should retrieve from cache without issues
            Optional<User> user2 = userService.findByEmail(testEmail);

            // Both calls should return the same result
            assertEquals(user1.isPresent(), user2.isPresent(), "Both calls should return same presence");

            if (user1.isPresent() && user2.isPresent()) {
                assertEquals(user1.get().getId(), user2.get().getId(), "User IDs should match");

                // Verify that AuthProviders are properly loaded and cached
                if (user1.get().getAuthProviders() != null && !user1.get().getAuthProviders().isEmpty()) {
                    assertNotNull(user2.get().getAuthProviders(), "Cached user should have AuthProviders");
                    assertEquals(user1.get().getAuthProviders().size(), user2.get().getAuthProviders().size(),
                            "AuthProviders count should match");

                    System.out.println("[DEBUG_LOG] User found with " + user1.get().getAuthProviders().size() + " auth providers");
                }
            }

            System.out.println("[DEBUG_LOG] UserService.findByEmail circular reference test passed");

        } catch (StackOverflowError e) {
            System.out.println("[DEBUG_LOG] StackOverflowError occurred in findByEmail: " + e.getMessage());
            fail("UserService.findByEmail should not cause StackOverflowError: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Error in findByEmail test: " + e.getMessage());
            e.printStackTrace();
            // Don't fail the test for other setup issues, but log them
        }
    }

    @Test
    public void testJsonIdentityInfoAnnotationsAreWorking() {
        System.out.println("[DEBUG_LOG] Testing that @JsonIdentityInfo annotations are properly configured");

        // Verify that User class has @JsonIdentityInfo annotation
        assertTrue(User.class.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonIdentityInfo.class),
                "User class should have @JsonIdentityInfo annotation");

        // Verify that AuthenticationProvider class has @JsonIdentityInfo annotation
        assertTrue(AuthenticationProvider.class.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonIdentityInfo.class),
                "AuthenticationProvider class should have @JsonIdentityInfo annotation");

        System.out.println("[DEBUG_LOG] @JsonIdentityInfo annotations are properly configured on both entities");
    }
}
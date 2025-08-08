package com.vladte.devhack.common.repository;

import com.vladte.devhack.common.repository.user.UserRepository;
import com.vladte.devhack.entities.enums.AuthProviderType;
import com.vladte.devhack.entities.user.AuthenticationProvider;
import com.vladte.devhack.entities.user.Profile;
import com.vladte.devhack.entities.user.User;
import com.vladte.devhack.entities.user.UserAccess;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the UserRepository.
 */
@DisplayName("User Repository Tests")
class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        // Clear any existing users
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save a user")
    @Description("Test that a user can be saved to the database")
    @Severity(SeverityLevel.BLOCKER)
    void testSaveUser() {
        // Arrange
        User user = createTestUser("testuser", "test@example.com");

        // Act
        User savedUser = userRepository.save(user);

        // Assert
        assertNotNull(savedUser.getId());
        assertNotNull(savedUser.getCreatedAt());
        assertEquals("testuser", savedUser.getProfile().getName());
        assertEquals("test@example.com", savedUser.getLocalAuth().get().getEmail());
    }

    @Test
    @DisplayName("Should find a user by ID")
    @Description("Test that a user can be retrieved by its ID")
    @Severity(SeverityLevel.CRITICAL)
    void testFindUserById() {
        // Arrange
        User user = createTestUser("testuser", "test@example.com");
        User savedUser = userRepository.save(user);
        UUID userId = savedUser.getId();

        // Act
        Optional<User> foundUser = userRepository.findById(userId);

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(userId, foundUser.get().getId());
        assertEquals("testuser", foundUser.get().getProfile().getName());
        assertEquals("test@example.com", foundUser.get().getLocalAuth().get().getEmail());
    }

    @Test
    @DisplayName("Should find a user by email")
    @Description("Test that a user can be retrieved by its email")
    @Severity(SeverityLevel.CRITICAL)
    void testFindUserByEmail() {
        // Arrange
        User user = createTestUser("testuser", "test@example.com");
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findByAuthProvidersEmail("test@example.com");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getProfile().getName());
        assertEquals("test@example.com", foundUser.get().getLocalAuth().get().getEmail());
    }

    @Test
    @DisplayName("Should find a user with profile by ID")
    @Description("Test that a user with profile can be retrieved by its ID")
    @Severity(SeverityLevel.CRITICAL)
    void testFindUserWithProfileById() {
        // Arrange
        User user = createTestUser("adminuser", "admin@example.com");
        user.getUserAccess().setRole("ADMIN");
        User savedUser = userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findWithProfileById(savedUser.getId());

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("adminuser", foundUser.get().getProfile().getName());
        assertEquals("admin@example.com", foundUser.get().getLocalAuth().get().getEmail());
        assertEquals("ADMIN", foundUser.get().getUserAccess().getRole());
    }

    @Test
    @DisplayName("Should update a user")
    @Description("Test that a user can be updated in the database")
    @Severity(SeverityLevel.CRITICAL)
    void testUpdateUser() {
        // Arrange
        User user = createTestUser("testuser", "test@example.com");
        User savedUser = userRepository.save(user);
        UUID userId = savedUser.getId();

        // Act
        savedUser.getProfile().setName("updateduser");
        savedUser.getLocalAuth().get().setEmail("updated@example.com");
        savedUser.getUserAccess().setRole("EDITOR");
        User updatedUser = userRepository.save(savedUser);

        // Assert
        assertEquals(userId, updatedUser.getId());
        assertEquals("updateduser", updatedUser.getProfile().getName());
        assertEquals("updated@example.com", updatedUser.getLocalAuth().get().getEmail());
        assertEquals("EDITOR", updatedUser.getUserAccess().getRole());
    }

    @Test
    @DisplayName("Should delete a user")
    @Description("Test that a user can be deleted from the database")
    @Severity(SeverityLevel.CRITICAL)
    void testDeleteUser() {
        // Arrange
        User user = createTestUser("testuser", "test@example.com");
        User savedUser = userRepository.save(user);
        UUID userId = savedUser.getId();

        // Act
        userRepository.deleteById(userId);

        // Assert
        Optional<User> foundUser = userRepository.findById(userId);
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("Should enforce unique email constraint")
    @Description("Test that the unique constraint on email is enforced")
    @Severity(SeverityLevel.CRITICAL)
    void testUniqueEmailConstraint() {
        // Arrange
        User user1 = createTestUser("user1", "same@example.com");
        userRepository.save(user1);

        User user2 = createTestUser("user2", "same@example.com");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user2);
            userRepository.flush(); // Force the constraint violation to be detected
        });
    }

    @Test
    @DisplayName("Should enforce unique name constraint")
    @Description("Test that the unique constraint on name is enforced")
    @Severity(SeverityLevel.CRITICAL)
    void testUniqueNameConstraint() {
        // Arrange
        User user1 = createTestUser("samename", "user1@example.com");
        userRepository.save(user1);

        User user2 = createTestUser("samename", "user2@example.com");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user2);
            userRepository.flush(); // Force the constraint violation to be detected
        });
    }

    /**
     * Helper method to create a test user.
     */
    private User createTestUser(String name, String email) {
        User user = new User();

        // Create profile
        Profile profile = new Profile();
        profile.setName(name);
        profile.setUser(user);
        user.setProfile(profile);

        // Create authentication provider
        AuthenticationProvider authProvider = new AuthenticationProvider();
        authProvider.setProvider(AuthProviderType.LOCAL);
        authProvider.setEmail(email);
        authProvider.setPasswordHash("password");
        authProvider.setUser(user);
        user.setAuthProviders(List.of(authProvider));

        // Create user access with default role
        UserAccess userAccess = new UserAccess();
        userAccess.setRole("USER");
        userAccess.setUser(user);
        user.setUserAccess(userAccess);

        return user;
    }
}
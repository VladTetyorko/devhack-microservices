package com.vladte.devhack.common.service.domain.user.impl;

import com.vladte.devhack.common.repository.user.UserRepository;
import com.vladte.devhack.common.service.BaseServiceTest;
import com.vladte.devhack.common.service.domain.audit.AuditService;
import com.vladte.devhack.common.service.domain.user.AuthenticationProviderService;
import com.vladte.devhack.common.service.domain.user.ProfileService;
import com.vladte.devhack.common.service.domain.user.UserAccessService;
import com.vladte.devhack.common.service.domain.user.UserService;
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
import org.mockito.Mock;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for UserServiceImpl.
 * Tests all user management functionality including registration, authentication, and user operations.
 */
@DisplayName("User Service Implementation Tests")
class UserServiceImplTest extends BaseServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationProviderService authProviderService;

    @Mock
    private ProfileService profileService;

    @Mock
    private UserAccessService userAccessService;

    @Mock
    private UserService self;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                auditService,
                passwordEncoder,
                authProviderService,
                profileService,
                userAccessService,
                self
        );
    }

    @Test
    @DisplayName("Should get existing system user")
    @Description("Test that getSystemUser returns existing system user when found")
    @Severity(SeverityLevel.CRITICAL)
    void testGetSystemUserExisting() {
        attachTestLog("Starting test: testGetSystemUserExisting");

        // Given
        User systemUser = createTestUser("system", "system@devhack.com");
        UserAccess systemAccess = new UserAccess();
        systemAccess.setRole("SYSTEM");
        systemAccess.setUser(systemUser);

        attachTestData(systemUser);
        attachTestData(systemAccess);

        when(userAccessService.findAllByRole("SYSTEM")).thenReturn(List.of(systemAccess));

        attachTestStep("Mock Setup", "Configured userAccessService to return existing system user");

        // When
        attachTestStep("Method Execution", "Calling userService.getSystemUser()");
        User result = userService.getSystemUser();

        // Then
        attachTestStep("Verification", "Verifying result is not null and equals expected system user");
        assertNotNull(result);
        assertEquals(systemUser, result);

        attachMethodExecution("getSystemUser", "no parameters", result.toString());

        verify(userAccessService).findAllByRole("SYSTEM");
        verify(userRepository, never()).save(any());

        attachTestLog("Test completed successfully: testGetSystemUserExisting");
    }

    @Test
    @DisplayName("Should create new system user when not found")
    @Description("Test that getSystemUser creates new system user when none exists")
    @Severity(SeverityLevel.CRITICAL)
    void testGetSystemUserCreateNew() {
        // Given
        User newSystemUser = createTestUser("system", "system@devhack.com");
        newSystemUser.setId(UUID.randomUUID());

        when(userAccessService.findAllByRole("SYSTEM")).thenReturn(List.of());
        when(userRepository.save(any(User.class))).thenReturn(newSystemUser);
        when(passwordEncoder.encode("system")).thenReturn("encoded_password");

        // When
        User result = userService.getSystemUser();

        // Then
        assertNotNull(result);
        verify(userAccessService).findAllByRole("SYSTEM");
        verify(userRepository).save(any(User.class));
        verify(authProviderService).save(any(AuthenticationProvider.class));
        verify(profileService).save(any(Profile.class));
        verify(userAccessService).save(any(UserAccess.class));
        verify(passwordEncoder).encode("system");
    }

    @Test
    @DisplayName("Should update user CV information")
    @Description("Test that updateUsersSv correctly updates user CV information")
    @Severity(SeverityLevel.NORMAL)
    void testUpdateUsersSv() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = createTestUser("testuser", "test@example.com");
        user.setId(userId);

        Profile profile = new Profile();
        profile.setUser(user);
        user.setProfile(profile);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        userService.updateUsersSv(user, "resume.pdf", "http://example.com/cv", "application/pdf");

        // Then
        assertEquals("resume.pdf", profile.getCvFileName());
        assertEquals("http://example.com/cv", profile.getCvFileHref());
        assertEquals("application/pdf", profile.getCvFileType());
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw exception when updating CV for non-existent user")
    @Description("Test that updateUsersSv throws exception when user not found")
    @Severity(SeverityLevel.NORMAL)
    void testUpdateUsersSvUserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = createTestUser("testuser", "test@example.com");
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUsersSv(user, "resume.pdf", "http://example.com/cv", "application/pdf");
        });
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should register regular user")
    @Description("Test that register creates a user with USER role")
    @Severity(SeverityLevel.CRITICAL)
    void testRegister() {
        // Given
        User inputUser = createTestUserWithAuth("testuser", "test@example.com", "password");
        User savedUser = createTestUser("testuser", "test@example.com");
        savedUser.setId(UUID.randomUUID());

        User systemUser = createTestUser("system", "system@devhack.com");

        when(self.getSystemUser()).thenReturn(systemUser);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");

        // When
        User result = userService.register(inputUser);

        // Then
        assertNotNull(result);
        assertEquals(savedUser, result);
        verify(self).getSystemUser();
        verify(userRepository).save(any(User.class));
        verify(authProviderService).save(any(AuthenticationProvider.class));
        verify(profileService).save(any(Profile.class));
        verify(userAccessService).save(any(UserAccess.class));
    }

    @Test
    @DisplayName("Should register manager user")
    @Description("Test that registerManager creates a user with MANAGER role")
    @Severity(SeverityLevel.CRITICAL)
    void testRegisterManager() {
        // Given
        User inputUser = createTestUserWithAuth("manager", "manager@example.com", "password");
        User savedUser = createTestUser("manager", "manager@example.com");
        savedUser.setId(UUID.randomUUID());

        User systemUser = createTestUser("system", "system@devhack.com");

        when(self.getSystemUser()).thenReturn(systemUser);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");

        // When
        User result = userService.registerManager(inputUser);

        // Then
        assertNotNull(result);
        assertEquals(savedUser, result);
        verify(self).getSystemUser();
        verify(userRepository).save(any(User.class));
        verify(authProviderService).save(any(AuthenticationProvider.class));
        verify(profileService).save(any(Profile.class));
        verify(userAccessService).save(any(UserAccess.class));
    }

    @Test
    @DisplayName("Should find user by email")
    @Description("Test that findByEmail returns user when found")
    @Severity(SeverityLevel.CRITICAL)
    void testFindByEmail() {
        // Given
        String email = "test@example.com";
        User user = createTestUser("testuser", email);
        AuthenticationProvider authProvider = new AuthenticationProvider();
        authProvider.setUser(user);
        authProvider.setEmail(email);

        when(authProviderService.findByProviderAndEmail(AuthProviderType.LOCAL, email))
                .thenReturn(Optional.of(authProvider));

        // When
        Optional<User> result = userService.findByEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(authProviderService).findByProviderAndEmail(AuthProviderType.LOCAL, email);
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    @Description("Test that findByEmail returns empty when user not found")
    @Severity(SeverityLevel.NORMAL)
    void testFindByEmailNotFound() {
        // Given
        String email = "nonexistent@example.com";

        when(authProviderService.findByProviderAndEmail(AuthProviderType.LOCAL, email))
                .thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail(email);

        // Then
        assertFalse(result.isPresent());
        verify(authProviderService).findByProviderAndEmail(AuthProviderType.LOCAL, email);
    }

    @Test
    @DisplayName("Should load user by username for authentication")
    @Description("Test that loadUserByUsername returns UserDetails for Spring Security")
    @Severity(SeverityLevel.CRITICAL)
    void testLoadUserByUsername() {
        // Given
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();

        User user = createTestUser("testuser", email);
        user.setId(userId);

        AuthenticationProvider authProvider = new AuthenticationProvider();
        authProvider.setUser(user);
        authProvider.setEmail(email);
        authProvider.setPasswordHash("encoded_password");

        UserAccess userAccess = new UserAccess();
        userAccess.setUser(user);
        userAccess.setRole("USER");
        userAccess.setIsAccountLocked(false);

        when(authProviderService.findByProviderAndEmail(AuthProviderType.LOCAL, email))
                .thenReturn(Optional.of(authProvider));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userAccessService.findByUserId(userId)).thenReturn(Optional.of(userAccess));

        // When
        UserDetails result = userService.loadUserByUsername(email);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getUsername());
        assertEquals("encoded_password", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));

        verify(authProviderService).findByProviderAndEmail(AuthProviderType.LOCAL, email);
        verify(userRepository).findById(userId);
        verify(userAccessService).findByUserId(userId);
    }

    @Test
    @DisplayName("Should throw exception when loading non-existent user")
    @Description("Test that loadUserByUsername throws UsernameNotFoundException when user not found")
    @Severity(SeverityLevel.NORMAL)
    void testLoadUserByUsernameNotFound() {
        // Given
        String email = "nonexistent@example.com";

        when(authProviderService.findByProviderAndEmail(AuthProviderType.LOCAL, email))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(email);
        });
        verify(authProviderService).findByProviderAndEmail(AuthProviderType.LOCAL, email);
    }

    @Test
    @DisplayName("Should handle locked user account")
    @Description("Test that loadUserByUsername handles locked user accounts correctly")
    @Severity(SeverityLevel.NORMAL)
    void testLoadUserByUsernameLockedAccount() {
        // Given
        String email = "locked@example.com";
        UUID userId = UUID.randomUUID();

        User user = createTestUser("lockeduser", email);
        user.setId(userId);

        AuthenticationProvider authProvider = new AuthenticationProvider();
        authProvider.setUser(user);
        authProvider.setEmail(email);
        authProvider.setPasswordHash("encoded_password");

        UserAccess userAccess = new UserAccess();
        userAccess.setUser(user);
        userAccess.setRole("USER");
        userAccess.setIsAccountLocked(true); // Account is locked

        when(authProviderService.findByProviderAndEmail(AuthProviderType.LOCAL, email))
                .thenReturn(Optional.of(authProvider));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userAccessService.findByUserId(userId)).thenReturn(Optional.of(userAccess));

        // When
        UserDetails result = userService.loadUserByUsername(email);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getUsername());
        assertFalse(result.isEnabled()); // Should be disabled due to lock
        assertFalse(result.isAccountNonLocked()); // Should be locked
    }

    /**
     * Helper method to create a test user.
     */
    private User createTestUser(String name, String email) {
        User user = new User();

        Profile profile = new Profile();
        profile.setName(name);
        profile.setUser(user);
        user.setProfile(profile);

        return user;
    }

    /**
     * Helper method to create a test user with authentication provider.
     */
    private User createTestUserWithAuth(String name, String email, String password) {
        User user = createTestUser(name, email);

        AuthenticationProvider authProvider = new AuthenticationProvider();
        authProvider.setProvider(AuthProviderType.LOCAL);
        authProvider.setEmail(email);
        authProvider.setPasswordHash(password);
        authProvider.setUser(user);
        user.setAuthProviders(List.of(authProvider));

        return user;
    }
}

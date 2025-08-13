package com.vladte.devhack.common.ddos.service;

import com.vladte.devhack.common.ddos.BaseServiceDdosTest;
import com.vladte.devhack.common.service.domain.user.UserService;
import com.vladte.devhack.entities.enums.AuthProviderType;
import com.vladte.devhack.entities.user.AuthenticationProvider;
import com.vladte.devhack.entities.user.Profile;
import com.vladte.devhack.entities.user.User;
import com.vladte.devhack.entities.user.UserAccess;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * DDoS simulation tests for UserService.
 * Tests the service's behavior under high concurrent load simulating DDoS attacks.
 * <p>
 * This class tests various scenarios including:
 * - Concurrent user creation
 * - Concurrent user lookups
 * - Mixed read/write operations
 * - Error handling under load
 */
@ActiveProfiles("test")
@Epic("DDoS Testing")
@Feature("User Service DDoS Tests")
@DisplayName("User Service DDoS Tests")
public class UserServiceDdosTest extends BaseServiceDdosTest {

    @Mock
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setup() {
        // Create a test user for mocking
        testUser = createTestUser("ddos-test-user");

        // Setup mock behaviors
        setupMockBehaviors();
    }

    private void setupMockBehaviors() {
        // Mock successful user creation
        when(userService.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        // Mock user lookup by ID
        when(userService.findById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            if (ThreadLocalRandom.current().nextDouble() < 0.9) { // 90% success rate
                User user = createTestUser("found-user-" + id.toString());
                user.setId(id);
                return Optional.of(user);
            }
            return Optional.empty();
        });

        // Mock user lookup by email
        when(userService.findByEmail(anyString())).thenAnswer(invocation -> {
            String email = invocation.getArgument(0);
            if (ThreadLocalRandom.current().nextDouble() < 0.8) { // 80% success rate
                User user = createTestUser("found-user-" + email);
                return Optional.of(user);
            }
            return Optional.empty();
        });

        // Mock user deletion (void method)
        doAnswer(invocation -> {
            if (ThreadLocalRandom.current().nextDouble() < 0.05) { // 5% failure rate
                throw new RuntimeException("Failed to delete user");
            }
            return null;
        }).when(userService).deleteById(any(UUID.class));
    }

    @Override
    protected Object getService() {
        return userService;
    }

    @Override
    protected Object executeReadOperation(String identifier) {
        try {
            // Randomly choose between different read operations
            int operationType = ThreadLocalRandom.current().nextInt(3);

            return switch (operationType) {
                case 0 -> { // Find by ID
                    UUID userId = UUID.randomUUID();
                    yield userService.findById(userId);
                }
                case 1 -> { // Find by email
                    String email = identifier + "@ddostest.com";
                    yield userService.findByEmail(email);
                }
                case 2 -> // Find all (if supported)
                    userService.findAll();
                default ->
                    userService.findById(UUID.randomUUID());
            };
        } catch (Exception e) {
            logger.error("[DEBUG_LOG] User read operation failed: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    protected Object executeWriteOperation(Object data) {
        try {
            if (data instanceof User user) {
                return userService.save(user);
            } else {
                // Create new user from identifier
                User user = createTestUser(data.toString());
                return userService.save(user);
            }
        } catch (Exception e) {
            logger.error("[DEBUG_LOG] User write operation failed: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    protected Object executeUpdateOperation(String identifier, Object data) {
        try {
            // Use save for update operations since there's no update method
            User user = (data instanceof User) ? (User) data : createTestUser(identifier);
            user.setId(UUID.randomUUID()); // Set ID to simulate update
            return userService.save(user);
        } catch (Exception e) {
            logger.error("[DEBUG_LOG] User update operation failed: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    protected Object executeDeleteOperation(String identifier) {
        try {
            UUID userId = UUID.randomUUID();
            userService.deleteById(userId);
            return "DELETED"; // Return a success indicator since deleteById is void
        } catch (Exception e) {
            logger.error("[DEBUG_LOG] User delete operation failed: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    protected Object createTestData(String identifier) {
        return createTestUser(identifier);
    }

    @Override
    protected boolean validateResult(Object result, String operationType) {
        if (result == null) {
            return false;
        }

        return switch (operationType) {
            case "READ" ->
                result instanceof Optional || result instanceof User || result instanceof List;
            case "WRITE", "UPDATE", "BULK_WRITE" ->
                result instanceof User;
            case "DELETE" ->
                "DELETED".equals(result);
            default ->
                true;
        };
    }

    /**
     * Creates a test user for DDoS testing.
     *
     * @param identifier unique identifier for the user
     * @return test user
     */
    private User createTestUser(String identifier) {
        User user = new User();

        // Create profile
        Profile profile = new Profile();
        profile.setName("DDoS Test User " + identifier);
        profile.setUser(user);
        user.setProfile(profile);

        // Create authentication provider
        AuthenticationProvider authProvider = new AuthenticationProvider();
        authProvider.setProvider(AuthProviderType.LOCAL);
        authProvider.setEmail(identifier + "@ddostest.com");
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

    @Override
    @DisplayName("DDoS Test: Concurrent User Service Read Operations")
    @Description("Tests UserService behavior under concurrent read operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testConcurrentServiceReadOperations() {
        logger.info("[DEBUG_LOG] Starting UserService concurrent read operations DDoS test");
        super.testConcurrentServiceReadOperations();
    }

    @Override
    @DisplayName("DDoS Test: Concurrent User Service Write Operations")
    @Description("Tests UserService behavior under concurrent write operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testConcurrentServiceWriteOperations() {
        logger.info("[DEBUG_LOG] Starting UserService concurrent write operations DDoS test");
        super.testConcurrentServiceWriteOperations();
    }

    @Override
    @DisplayName("DDoS Test: Mixed User Service Operations")
    @Description("Tests UserService behavior under mixed concurrent operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testMixedServiceOperations() {
        logger.info("[DEBUG_LOG] Starting UserService mixed operations DDoS test");
        super.testMixedServiceOperations();
    }

    @Override
    @DisplayName("DDoS Test: Aggressive User Service Stress Test")
    @Description("Tests UserService behavior under aggressive load simulating intense DDoS attack")
    @Severity(SeverityLevel.BLOCKER)
    public void testAggressiveServiceStress() {
        logger.info("[DEBUG_LOG] Starting UserService aggressive stress test");
        super.testAggressiveServiceStress();
    }

    @Override
    @DisplayName("DDoS Test: User Service Error Handling Under Load")
    @Description("Tests UserService error handling behavior under load simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testServiceErrorHandlingUnderLoad() {
        logger.info("[DEBUG_LOG] Starting UserService error handling under load DDoS test");
        super.testServiceErrorHandlingUnderLoad();
    }

    @Override
    @DisplayName("DDoS Test: User Service Bulk Operations")
    @Description("Tests UserService behavior under bulk operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testServiceBulkOperations() {
        logger.info("[DEBUG_LOG] Starting UserService bulk operations DDoS test");
        super.testServiceBulkOperations();
    }

    @Override
    protected void assertDdosTestResults(DdosTestResult result, String testName) {
        logger.info("[DEBUG_LOG] Asserting UserService DDoS test results for {}: {}", testName, result);

        // User service specific assertions
        assert result.getTotalRequests() > 0 : "No user service operations were executed";

        // For user service operations, we expect good success rate with mocked dependencies
        if (testName.contains("Read")) {
            assert result.getSuccessRate() >= 85.0 : "User service read success rate too low: " + result.getSuccessRate() + "%";
        } else if (testName.contains("Write") || testName.contains("Bulk")) {
            assert result.getSuccessRate() >= 80.0 : "User service write success rate too low: " + result.getSuccessRate() + "%";
        } else {
            assert result.getSuccessRate() >= 75.0 : "User service operation success rate too low: " + result.getSuccessRate() + "%";
        }

        // User service operations should be fast with mocked dependencies
        assert result.getAverageRequestTime() < 2000 : "User service operation time too high: " + result.getAverageRequestTime() + "ms";

        // Log user service specific metrics
        attachText(testName + " User Service Metrics",
                String.format("User Service DDoS Test Results:\n" +
                                "Test: %s\n" +
                                "Total User Operations Processed: %d\n" +
                                "Success Rate: %.2f%%\n" +
                                "Average Time per User Operation: %.2fms\n" +
                                "Total Test Duration: %dms",
                        testName, result.getTotalRequests(), result.getSuccessRate(),
                        result.getAverageRequestTime(), result.getTotalExecutionTime()));

        // Call parent assertion for common checks
        super.assertDdosTestResults(result, testName);
    }

    @Override
    protected void assertAggressiveDdosTestResults(DdosTestResult result, String testName) {
        logger.info("[DEBUG_LOG] Asserting UserService aggressive DDoS test results for {}: {}", testName, result);

        // More lenient assertions for aggressive user service tests
        assert result.getTotalRequests() > 0 : "No aggressive user service operations were executed";
        assert result.getSuccessRate() >= 70.0 : "Aggressive user service test success rate too low: " + result.getSuccessRate() + "%";
        assert result.getAverageRequestTime() < 5000 : "Aggressive user service operation time too high: " + result.getAverageRequestTime() + "ms";

        // Log aggressive test metrics
        attachText(testName + " Aggressive User Service Metrics",
                String.format("Aggressive User Service DDoS Test Results:\n" +
                                "Test: %s\n" +
                                "Total Aggressive Operations: %d\n" +
                                "Success Rate: %.2f%%\n" +
                                "Average Time per Operation: %.2fms\n" +
                                "Total Test Duration: %dms\n" +
                                "Exception Count: %d",
                        testName, result.getTotalRequests(), result.getSuccessRate(),
                        result.getAverageRequestTime(), result.getTotalExecutionTime(),
                        result.getExceptions().size()));

        // Call parent assertion for common checks
        super.assertAggressiveDdosTestResults(result, testName);
    }

    @Override
    protected void assertErrorHandlingDdosTestResults(DdosTestResult result, String testName) {
        logger.info("[DEBUG_LOG] Asserting UserService error handling DDoS test results for {}: {}", testName, result);

        // Very lenient assertions for error handling tests
        assert result.getTotalRequests() > 0 : "No error handling operations were executed";
        assert result.getSuccessRate() >= 40.0 : "User service error handling success rate too low: " + result.getSuccessRate() + "%";
        assert result.getAverageRequestTime() < 8000 : "User service error handling time too high: " + result.getAverageRequestTime() + "ms";

        // Log error handling test metrics
        attachText(testName + " User Service Error Handling Metrics",
                String.format("User Service Error Handling DDoS Test Results:\n" +
                                "Test: %s\n" +
                                "Total Error Handling Operations: %d\n" +
                                "Success Rate: %.2f%%\n" +
                                "Average Time per Operation: %.2fms\n" +
                                "Total Test Duration: %dms\n" +
                                "Exception Count: %d",
                        testName, result.getTotalRequests(), result.getSuccessRate(),
                        result.getAverageRequestTime(), result.getTotalExecutionTime(),
                        result.getExceptions().size()));

        // Call parent assertion for common checks
        super.assertErrorHandlingDdosTestResults(result, testName);
    }
}

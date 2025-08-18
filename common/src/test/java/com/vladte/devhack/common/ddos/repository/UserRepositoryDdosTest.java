package com.vladte.devhack.common.ddos.repository;

import com.vladte.devhack.common.ddos.BaseRepositoryDdosTest;
import com.vladte.devhack.common.repository.user.UserRepository;
import com.vladte.devhack.entities.enums.AuthProviderType;
import com.vladte.devhack.entities.user.AuthenticationProvider;
import com.vladte.devhack.entities.user.Profile;
import com.vladte.devhack.entities.user.User;
import com.vladte.devhack.entities.user.UserAccess;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DDoS simulation tests for UserRepository.
 * Tests the repository's behavior under high concurrent load simulating DDoS attacks.
 * <p>
 * This class tests various scenarios including:
 * - Concurrent user creation
 * - Concurrent user lookups
 * - Mixed read/write operations
 * - Bulk operations under load
 */
@DataJpaTest
@ActiveProfiles("test")
@Epic("DDoS Testing")
@Feature("User Repository DDoS Tests")
@DisplayName("User Repository DDoS Tests")
public class UserRepositoryDdosTest extends BaseRepositoryDdosTest<User, UUID> {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected JpaRepository<User, UUID> getRepository() {
        return userRepository;
    }

    @Override
    protected User createTestEntity() {
        return createTestEntity("ddos-user-" + UUID.randomUUID());
    }

    @Override
    protected User createTestEntity(String identifier) {
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
    protected UUID getEntityId(User entity) {
        return entity.getId();
    }

    @Override
    protected User updateEntity(User entity) {
        entity.setUpdatedAt(LocalDateTime.now());
        if (entity.getProfile() != null) {
            entity.getProfile().setName("Updated " + entity.getProfile().getName());
            entity.getProfile().setAiSkillsSummary("Updated skills summary for DDoS test");
        }
        if (entity.getUserAccess() != null) {
            entity.getUserAccess().setRole("UPDATED_USER");
        }
        return entity;
    }

    @Override
    @DisplayName("DDoS Test: Concurrent User Read Operations")
    @Description("Tests UserRepository behavior under concurrent read operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testConcurrentReadOperations() {
        logger.info("[DEBUG_LOG] Starting UserRepository concurrent read operations DDoS test");
        super.testConcurrentReadOperations();
    }

    @Override
    @DisplayName("DDoS Test: Concurrent User Write Operations")
    @Description("Tests UserRepository behavior under concurrent write operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testConcurrentWriteOperations() {
        logger.info("[DEBUG_LOG] Starting UserRepository concurrent write operations DDoS test");
        super.testConcurrentWriteOperations();
    }

    @Override
    @DisplayName("DDoS Test: Mixed User Read/Write Operations")
    @Description("Tests UserRepository behavior under mixed concurrent read/write operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testMixedReadWriteOperations() {
        logger.info("[DEBUG_LOG] Starting UserRepository mixed read/write operations DDoS test");
        super.testMixedReadWriteOperations();
    }

    @Override
    @DisplayName("DDoS Test: Aggressive User Repository Stress Test")
    @Description("Tests UserRepository behavior under aggressive load simulating intense DDoS attack")
    @Severity(SeverityLevel.BLOCKER)
    public void testAggressiveRepositoryStress() {
        logger.info("[DEBUG_LOG] Starting UserRepository aggressive stress test");
        super.testAggressiveRepositoryStress();
    }

    @Override
    @DisplayName("DDoS Test: User Repository Bulk Operations")
    @Description("Tests UserRepository behavior under bulk operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testBulkOperations() {
        logger.info("[DEBUG_LOG] Starting UserRepository bulk operations DDoS test");
        super.testBulkOperations();
    }

    @Override
    protected void assertDdosTestResults(DdosTestResult result, String testName) {
        logger.info("[DEBUG_LOG] Asserting UserRepository DDoS test results for {}: {}", testName, result);

        // User-specific assertions
        assert result.getTotalRequests() > 0 : "No user operations were executed";

        // For user operations, we expect high success rate due to simple operations
        if (testName.contains("Read")) {
            assert result.getSuccessRate() >= 90.0 : "User read success rate too low: " + result.getSuccessRate() + "%";
        } else if (testName.contains("Write") || testName.contains("Bulk")) {
            assert result.getSuccessRate() >= 85.0 : "User write success rate too low: " + result.getSuccessRate() + "%";
        } else {
            assert result.getSuccessRate() >= 80.0 : "User operation success rate too low: " + result.getSuccessRate() + "%";
        }

        // User operations should be relatively fast
        assert result.getAverageRequestTime() < 3000 : "User operation time too high: " + result.getAverageRequestTime() + "ms";

        // Log user-specific metrics
        attachText(testName + " User Metrics",
                String.format("User Repository DDoS Test Results:\n" +
                                "Test: %s\n" +
                                "Total Users Processed: %d\n" +
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
        logger.info("[DEBUG_LOG] Asserting UserRepository aggressive DDoS test results for {}: {}", testName, result);

        // More lenient assertions for aggressive user tests
        assert result.getTotalRequests() > 0 : "No aggressive user operations were executed";
        assert result.getSuccessRate() >= 70.0 : "Aggressive user test success rate too low: " + result.getSuccessRate() + "%";
        assert result.getAverageRequestTime() < 8000 : "Aggressive user operation time too high: " + result.getAverageRequestTime() + "ms";

        // Log aggressive test metrics
        attachText(testName + " Aggressive User Metrics",
                String.format("Aggressive User Repository DDoS Test Results:\n" +
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
}

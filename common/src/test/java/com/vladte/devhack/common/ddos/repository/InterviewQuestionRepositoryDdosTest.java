package com.vladte.devhack.common.ddos.repository;

import com.vladte.devhack.common.ddos.BaseRepositoryDdosTest;
import com.vladte.devhack.domain.entities.enums.AuthProviderType;
import com.vladte.devhack.domain.entities.global.InterviewQuestion;
import com.vladte.devhack.domain.entities.user.AuthenticationProvider;
import com.vladte.devhack.domain.entities.user.Profile;
import com.vladte.devhack.domain.entities.user.User;
import com.vladte.devhack.domain.repository.global.InterviewQuestionRepository;
import com.vladte.devhack.domain.repository.user.UserRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * DDoS simulation tests for InterviewQuestionRepository.
 * Tests the repository's behavior under high concurrent load simulating DDoS attacks.
 * <p>
 * This class tests various scenarios including:
 * - Concurrent question creation
 * - Concurrent question lookups
 * - Mixed read/write operations
 * - Bulk operations under load
 */
@DataJpaTest
@ActiveProfiles("test")
@Epic("DDoS Testing")
@Feature("Interview Question Repository DDoS Tests")
@DisplayName("Interview Question Repository DDoS Tests")
public class InterviewQuestionRepositoryDdosTest extends BaseRepositoryDdosTest<InterviewQuestion, UUID> {

    @Autowired
    private InterviewQuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        // Create and save a test user for questions
        testUser = new User();

        // Create profile
        Profile profile = new Profile();
        profile.setName("ddos-test-user");
        profile.setUser(testUser);
        testUser.setProfile(profile);

        // Create authentication provider
        AuthenticationProvider authProvider = new AuthenticationProvider();
        authProvider.setProvider(AuthProviderType.LOCAL);
        authProvider.setEmail("ddostest@example.com");
        authProvider.setPasswordHash("password");
        authProvider.setUser(testUser);
        testUser.setAuthProviders(List.of(authProvider));

        userRepository.save(testUser);

        // Clear any existing questions
        questionRepository.deleteAll();
    }

    @Override
    protected JpaRepository<InterviewQuestion, UUID> getRepository() {
        return questionRepository;
    }

    @Override
    protected InterviewQuestion createTestEntity() {
        return createTestEntity("ddos-question-" + UUID.randomUUID());
    }

    @Override
    protected InterviewQuestion createTestEntity(String identifier) {
        InterviewQuestion question = new InterviewQuestion();
        question.setQuestionText("DDoS Test Question: " + identifier + "?");
        question.setDifficulty(getRandomDifficulty());
        question.setSource("DDoS Test Suite");
        question.setUser(testUser);
        return question;
    }

    @Override
    protected UUID getEntityId(InterviewQuestion entity) {
        return entity.getId();
    }

    @Override
    protected InterviewQuestion updateEntity(InterviewQuestion entity) {
        entity.setQuestionText("Updated: " + entity.getQuestionText());
        entity.setDifficulty(getRandomDifficulty());
        entity.setSource("Updated DDoS Test Suite");
        return entity;
    }

    /**
     * Gets a random difficulty level for test questions.
     *
     * @return random difficulty level
     */
    private String getRandomDifficulty() {
        String[] difficulties = {"Easy", "Medium", "Hard"};
        return difficulties[ThreadLocalRandom.current().nextInt(difficulties.length)];
    }

    @Override
    @DisplayName("DDoS Test: Concurrent Question Read Operations")
    @Description("Tests InterviewQuestionRepository behavior under concurrent read operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testConcurrentReadOperations() {
        logger.info("[DEBUG_LOG] Starting InterviewQuestionRepository concurrent read operations DDoS test");
        super.testConcurrentReadOperations();
    }

    @Override
    @DisplayName("DDoS Test: Concurrent Question Write Operations")
    @Description("Tests InterviewQuestionRepository behavior under concurrent write operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testConcurrentWriteOperations() {
        logger.info("[DEBUG_LOG] Starting InterviewQuestionRepository concurrent write operations DDoS test");
        super.testConcurrentWriteOperations();
    }

    @Override
    @DisplayName("DDoS Test: Mixed Question Read/Write Operations")
    @Description("Tests InterviewQuestionRepository behavior under mixed concurrent read/write operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testMixedReadWriteOperations() {
        logger.info("[DEBUG_LOG] Starting InterviewQuestionRepository mixed read/write operations DDoS test");
        super.testMixedReadWriteOperations();
    }

    @Override
    @DisplayName("DDoS Test: Aggressive Question Repository Stress Test")
    @Description("Tests InterviewQuestionRepository behavior under aggressive load simulating intense DDoS attack")
    @Severity(SeverityLevel.BLOCKER)
    public void testAggressiveRepositoryStress() {
        logger.info("[DEBUG_LOG] Starting InterviewQuestionRepository aggressive stress test");
        super.testAggressiveRepositoryStress();
    }

    @Override
    @DisplayName("DDoS Test: Question Repository Bulk Operations")
    @Description("Tests InterviewQuestionRepository behavior under bulk operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testBulkOperations() {
        logger.info("[DEBUG_LOG] Starting InterviewQuestionRepository bulk operations DDoS test");
        super.testBulkOperations();
    }

    @Override
    protected void assertDdosTestResults(DdosTestResult result, String testName) {
        logger.info("[DEBUG_LOG] Asserting InterviewQuestionRepository DDoS test results for {}: {}", testName, result);

        // Question-specific assertions
        assert result.getTotalRequests() > 0 : "No question operations were executed";

        // For question operations, we expect good success rate
        if (testName.contains("Read")) {
            assert result.getSuccessRate() >= 85.0 : "Question read success rate too low: " + result.getSuccessRate() + "%";
        } else if (testName.contains("Write") || testName.contains("Bulk")) {
            assert result.getSuccessRate() >= 80.0 : "Question write success rate too low: " + result.getSuccessRate() + "%";
        } else {
            assert result.getSuccessRate() >= 75.0 : "Question operation success rate too low: " + result.getSuccessRate() + "%";
        }

        // Question operations should be reasonably fast
        assert result.getAverageRequestTime() < 4000 : "Question operation time too high: " + result.getAverageRequestTime() + "ms";

        // Log question-specific metrics
        attachText(testName + " Question Metrics",
                String.format("Interview Question Repository DDoS Test Results:\n" +
                                "Test: %s\n" +
                                "Total Questions Processed: %d\n" +
                                "Success Rate: %.2f%%\n" +
                                "Average Time per Question Operation: %.2fms\n" +
                                "Total Test Duration: %dms",
                        testName, result.getTotalRequests(), result.getSuccessRate(),
                        result.getAverageRequestTime(), result.getTotalExecutionTime()));

        // Call parent assertion for common checks
        super.assertDdosTestResults(result, testName);
    }

    @Override
    protected void assertAggressiveDdosTestResults(DdosTestResult result, String testName) {
        logger.info("[DEBUG_LOG] Asserting InterviewQuestionRepository aggressive DDoS test results for {}: {}", testName, result);

        // More lenient assertions for aggressive question tests
        assert result.getTotalRequests() > 0 : "No aggressive question operations were executed";
        assert result.getSuccessRate() >= 65.0 : "Aggressive question test success rate too low: " + result.getSuccessRate() + "%";
        assert result.getAverageRequestTime() < 10000 : "Aggressive question operation time too high: " + result.getAverageRequestTime() + "ms";

        // Log aggressive test metrics
        attachText(testName + " Aggressive Question Metrics",
                String.format("Aggressive Interview Question Repository DDoS Test Results:\n" +
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
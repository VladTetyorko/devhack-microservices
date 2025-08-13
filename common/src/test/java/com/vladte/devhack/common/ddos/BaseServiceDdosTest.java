package com.vladte.devhack.common.ddos;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Base class for DDoS simulation tests on services.
 * Extends both BaseDdosTest and provides service-specific testing capabilities.
 * <p>
 * This class provides common DDoS test scenarios for services including:
 * - Concurrent service method calls
 * - Mixed operation patterns
 * - Stress testing with high load
 * - Error handling under load
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseServiceDdosTest extends BaseDdosTest {

    /**
     * Gets the service instance to test.
     * Must be implemented by concrete test classes.
     *
     * @return the service instance
     */
    protected abstract Object getService();

    /**
     * Executes a read operation on the service.
     * Must be implemented by concrete test classes.
     *
     * @param identifier optional identifier for the read operation
     * @return the result of the read operation
     */
    protected abstract Object executeReadOperation(String identifier);

    /**
     * Executes a write operation on the service.
     * Must be implemented by concrete test classes.
     *
     * @param data optional data for the write operation
     * @return the result of the write operation
     */
    protected abstract Object executeWriteOperation(Object data);

    /**
     * Executes an update operation on the service.
     * Default implementation calls executeWriteOperation.
     *
     * @param identifier identifier for the entity to update
     * @param data       data for the update operation
     * @return the result of the update operation
     */
    protected Object executeUpdateOperation(String identifier, Object data) {
        return executeWriteOperation(data);
    }

    /**
     * Executes a delete operation on the service.
     * Default implementation throws UnsupportedOperationException.
     *
     * @param identifier identifier for the entity to delete
     * @return the result of the delete operation
     */
    protected Object executeDeleteOperation(String identifier) {
        throw new UnsupportedOperationException("Delete operation not implemented");
    }

    /**
     * Creates test data for service operations.
     * Must be implemented by concrete test classes.
     *
     * @param identifier unique identifier for the test data
     * @return test data object
     */
    protected abstract Object createTestData(String identifier);

    /**
     * Validates the result of a service operation.
     * Default implementation checks for non-null result.
     *
     * @param result        the operation result
     * @param operationType the type of operation performed
     * @return true if result is valid
     */
    protected boolean validateResult(Object result, String operationType) {
        return result != null;
    }

    @Test
    @DisplayName("DDoS Test: Concurrent Service Read Operations")
    @Description("Tests service behavior under concurrent read operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testConcurrentServiceReadOperations() {
        logger.info("[DEBUG_LOG] Starting concurrent service read operations DDoS test");

        // Pre-create test identifiers
        List<String> testIdentifiers = createTestIdentifiers(10);

        attachText("Test Data", "Created " + testIdentifiers.size() + " test identifiers for concurrent read test");

        // Execute concurrent read operations
        Supplier<Void> readOperation = () -> {
            try {
                // Random read operation
                String randomId = testIdentifiers.get(ThreadLocalRandom.current().nextInt(testIdentifiers.size()));
                Object result = executeReadOperation(randomId);

                // Validate result
                if (!validateResult(result, "READ")) {
                    throw new RuntimeException("Invalid read result for identifier: " + randomId);
                }

                return null;
            } catch (Exception e) {
                logger.error("[DEBUG_LOG] Service read operation failed: {}", e.getMessage());
                throw e;
            }
        };

        DdosTestResult result = executeDdosTest(readOperation);

        // Verify results
        assertDdosTestResults(result, "Concurrent Service Read Operations");

        logger.info("[DEBUG_LOG] Concurrent service read operations DDoS test completed: {}", result);
    }

    @Test
    @DisplayName("DDoS Test: Concurrent Service Write Operations")
    @Description("Tests service behavior under concurrent write operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testConcurrentServiceWriteOperations() {
        logger.info("[DEBUG_LOG] Starting concurrent service write operations DDoS test");

        // Execute concurrent write operations
        Supplier<Void> writeOperation = () -> {
            try {
                // Create test data and execute write operation
                Object testData = createTestData("ddos-write-" + UUID.randomUUID());
                Object result = executeWriteOperation(testData);

                // Validate result
                if (!validateResult(result, "WRITE")) {
                    throw new RuntimeException("Invalid write result");
                }

                return null;
            } catch (Exception e) {
                logger.error("[DEBUG_LOG] Service write operation failed: {}", e.getMessage());
                throw e;
            }
        };

        DdosTestResult result = executeDdosTest(writeOperation);

        // Verify results
        assertDdosTestResults(result, "Concurrent Service Write Operations");

        logger.info("[DEBUG_LOG] Concurrent service write operations DDoS test completed: {}", result);
    }

    @Test
    @DisplayName("DDoS Test: Mixed Service Operations")
    @Description("Tests service behavior under mixed concurrent operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testMixedServiceOperations() {
        logger.info("[DEBUG_LOG] Starting mixed service operations DDoS test");

        // Pre-create test identifiers
        List<String> testIdentifiers = createTestIdentifiers(5);

        // Execute mixed operations
        Supplier<Void> mixedOperation = () -> {
            try {
                boolean isReadOperation = ThreadLocalRandom.current().nextBoolean();

                if (isReadOperation && !testIdentifiers.isEmpty()) {
                    // Read operation
                    String randomId = testIdentifiers.get(ThreadLocalRandom.current().nextInt(testIdentifiers.size()));
                    Object result = executeReadOperation(randomId);

                    if (!validateResult(result, "READ")) {
                        throw new RuntimeException("Invalid read result for identifier: " + randomId);
                    }
                } else {
                    // Write operation
                    String identifier = "mixed-" + UUID.randomUUID();
                    Object testData = createTestData(identifier);
                    Object result = executeWriteOperation(testData);

                    if (!validateResult(result, "WRITE")) {
                        throw new RuntimeException("Invalid write result");
                    }

                    // Add identifier for future reads (thread-safe)
                    synchronized (testIdentifiers) {
                        testIdentifiers.add(identifier);
                    }
                }

                return null;
            } catch (Exception e) {
                logger.error("[DEBUG_LOG] Mixed service operation failed: {}", e.getMessage());
                throw e;
            }
        };

        DdosTestResult result = executeDdosTest(mixedOperation);

        // Verify results
        assertDdosTestResults(result, "Mixed Service Operations");

        logger.info("[DEBUG_LOG] Mixed service operations DDoS test completed: {}", result);
    }

    @Test
    @DisplayName("DDoS Test: Aggressive Service Stress Test")
    @Description("Tests service behavior under aggressive load simulating intense DDoS attack")
    @Severity(SeverityLevel.BLOCKER)
    public void testAggressiveServiceStress() {
        logger.info("[DEBUG_LOG] Starting aggressive service stress test");

        // Pre-create test identifiers
        List<String> testIdentifiers = createTestIdentifiers(20);

        // Execute aggressive mixed operations
        Supplier<Void> aggressiveOperation = () -> {
            try {
                int operationType = ThreadLocalRandom.current().nextInt(3);

                switch (operationType) {
                    case 0: // Read operation
                        if (!testIdentifiers.isEmpty()) {
                            String randomId = testIdentifiers.get(ThreadLocalRandom.current().nextInt(testIdentifiers.size()));
                            Object result = executeReadOperation(randomId);
                            validateResult(result, "READ");
                        }
                        break;

                    case 1: // Write operation
                        String identifier = "aggressive-" + UUID.randomUUID();
                        Object testData = createTestData(identifier);
                        Object result = executeWriteOperation(testData);
                        validateResult(result, "WRITE");
                        synchronized (testIdentifiers) {
                            testIdentifiers.add(identifier);
                        }
                        break;

                    case 2: // Update operation
                        if (!testIdentifiers.isEmpty()) {
                            String randomId = testIdentifiers.get(ThreadLocalRandom.current().nextInt(testIdentifiers.size()));
                            Object updateData = createTestData("update-" + randomId);
                            try {
                                Object updateResult = executeUpdateOperation(randomId, updateData);
                                validateResult(updateResult, "UPDATE");
                            } catch (UnsupportedOperationException e) {
                                // Update operation not supported, skip
                                logger.debug("[DEBUG_LOG] Update operation not supported, skipping");
                            }
                        }
                        break;
                }

                return null;
            } catch (Exception e) {
                logger.error("[DEBUG_LOG] Aggressive service operation failed: {}", e.getMessage());
                throw e;
            }
        };

        DdosTestResult result = executeAggressiveDdosTest(aggressiveOperation);

        // Verify results (more lenient for aggressive tests)
        assertAggressiveDdosTestResults(result, "Aggressive Service Stress Test");

        logger.info("[DEBUG_LOG] Aggressive service stress test completed: {}", result);
    }

    @Test
    @DisplayName("DDoS Test: Service Error Handling Under Load")
    @Description("Tests service error handling behavior under load simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testServiceErrorHandlingUnderLoad() {
        logger.info("[DEBUG_LOG] Starting service error handling under load DDoS test");

        // Execute operations that may cause errors
        Supplier<Void> errorProneOperation = () -> {
            try {
                // Mix of valid and invalid operations
                boolean causeError = ThreadLocalRandom.current().nextDouble() < 0.3; // 30% error rate

                if (causeError) {
                    // Execute operation that should cause an error
                    executeReadOperation("non-existent-" + UUID.randomUUID());
                } else {
                    // Execute valid operation
                    Object testData = createTestData("error-test-" + UUID.randomUUID());
                    Object result = executeWriteOperation(testData);
                    validateResult(result, "WRITE");
                }

                return null;
            } catch (Exception e) {
                // Expected for error-prone operations
                logger.debug("[DEBUG_LOG] Expected error in error handling test: {}", e.getMessage());
                throw e;
            }
        };

        DdosTestResult result = executeDdosTest(errorProneOperation);

        // Verify results (more lenient for error handling tests)
        assertErrorHandlingDdosTestResults(result, "Service Error Handling Under Load");

        logger.info("[DEBUG_LOG] Service error handling under load DDoS test completed: {}", result);
    }

    @Test
    @DisplayName("DDoS Test: Service Bulk Operations")
    @Description("Tests service behavior under bulk operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testServiceBulkOperations() {
        logger.info("[DEBUG_LOG] Starting service bulk operations DDoS test");

        // Execute bulk operations
        Supplier<Void> bulkOperation = () -> {
            try {
                // Create multiple test data items and process them
                List<Object> testDataList = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    testDataList.add(createTestData("bulk-" + UUID.randomUUID() + "-" + i));
                }

                // Process each item (simulating bulk operation)
                for (Object testData : testDataList) {
                    Object result = executeWriteOperation(testData);
                    if (!validateResult(result, "BULK_WRITE")) {
                        throw new RuntimeException("Bulk operation failed for item: " + testData);
                    }
                }

                return null;
            } catch (Exception e) {
                logger.error("[DEBUG_LOG] Service bulk operation failed: {}", e.getMessage());
                throw e;
            }
        };

        DdosTestResult result = executeDdosTest(bulkOperation, 20, 10); // Reduced load for bulk operations

        // Verify results
        assertDdosTestResults(result, "Service Bulk Operations");

        logger.info("[DEBUG_LOG] Service bulk operations DDoS test completed: {}", result);
    }

    /**
     * Creates test identifiers for DDoS testing.
     *
     * @param count number of test identifiers to create
     * @return list of test identifiers
     */
    protected List<String> createTestIdentifiers(int count) {
        List<String> identifiers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            identifiers.add("test-id-" + i + "-" + UUID.randomUUID());
        }
        return identifiers;
    }

    /**
     * Asserts DDoS test results meet minimum requirements.
     *
     * @param result   the test result
     * @param testName the name of the test
     */
    @Step("Asserting service DDoS test results for {testName}")
    protected void assertDdosTestResults(DdosTestResult result, String testName) {
        logger.info("[DEBUG_LOG] Asserting service DDoS test results for {}: {}", testName, result);

        // Basic assertions
        assert result.getTotalRequests() > 0 : "No requests were executed";
        assert result.getSuccessRate() >= 80.0 : "Success rate too low: " + result.getSuccessRate() + "%";
        assert result.getAverageRequestTime() < 5000 : "Average request time too high: " + result.getAverageRequestTime() + "ms";

        // Log results
        attachText(testName + " Results", result.toString());

        if (!result.getExceptions().isEmpty()) {
            StringBuilder exceptionDetails = new StringBuilder();
            result.getExceptions().stream().limit(5).forEach(e ->
                    exceptionDetails.append(e.getClass().getSimpleName())
                            .append(": ").append(e.getMessage()).append("\n"));
            attachText(testName + " Exceptions", exceptionDetails.toString());
        }
    }

    /**
     * Asserts aggressive DDoS test results with more lenient requirements.
     *
     * @param result   the test result
     * @param testName the name of the test
     */
    @Step("Asserting aggressive service DDoS test results for {testName}")
    protected void assertAggressiveDdosTestResults(DdosTestResult result, String testName) {
        logger.info("[DEBUG_LOG] Asserting aggressive service DDoS test results for {}: {}", testName, result);

        // More lenient assertions for aggressive tests
        assert result.getTotalRequests() > 0 : "No requests were executed";
        assert result.getSuccessRate() >= 60.0 : "Success rate too low for aggressive test: " + result.getSuccessRate() + "%";
        assert result.getAverageRequestTime() < 10000 : "Average request time too high: " + result.getAverageRequestTime() + "ms";

        // Log results
        attachText(testName + " Results", result.toString());

        if (!result.getExceptions().isEmpty()) {
            StringBuilder exceptionDetails = new StringBuilder();
            result.getExceptions().stream().limit(10).forEach(e ->
                    exceptionDetails.append(e.getClass().getSimpleName())
                            .append(": ").append(e.getMessage()).append("\n"));
            attachText(testName + " Exceptions", exceptionDetails.toString());
        }
    }

    /**
     * Asserts error handling DDoS test results with very lenient requirements.
     *
     * @param result   the test result
     * @param testName the name of the test
     */
    @Step("Asserting error handling DDoS test results for {testName}")
    protected void assertErrorHandlingDdosTestResults(DdosTestResult result, String testName) {
        logger.info("[DEBUG_LOG] Asserting error handling DDoS test results for {}: {}", testName, result);

        // Very lenient assertions for error handling tests
        assert result.getTotalRequests() > 0 : "No requests were executed";
        assert result.getSuccessRate() >= 30.0 : "Success rate too low for error handling test: " + result.getSuccessRate() + "%";
        assert result.getAverageRequestTime() < 15000 : "Average request time too high: " + result.getAverageRequestTime() + "ms";

        // Log results
        attachText(testName + " Results", result.toString());

        // Error handling tests are expected to have exceptions
        if (!result.getExceptions().isEmpty()) {
            StringBuilder exceptionDetails = new StringBuilder();
            result.getExceptions().stream().limit(15).forEach(e ->
                    exceptionDetails.append(e.getClass().getSimpleName())
                            .append(": ").append(e.getMessage()).append("\n"));
            attachText(testName + " Expected Exceptions", exceptionDetails.toString());
        }
    }
}
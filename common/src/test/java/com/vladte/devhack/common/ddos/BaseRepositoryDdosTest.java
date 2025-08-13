package com.vladte.devhack.common.ddos;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Base class for DDoS simulation tests on repositories.
 * Extends both BaseDdosTest and BaseRepositoryTest to provide comprehensive testing capabilities.
 * <p>
 * This class provides common DDoS test scenarios for repositories including:
 * - Concurrent read operations
 * - Concurrent write operations
 * - Mixed read/write operations
 * - Stress testing with high load
 */
public abstract class BaseRepositoryDdosTest<T, ID> extends BaseDdosTest {

    /**
     * Gets the repository instance to test.
     * Must be implemented by concrete test classes.
     *
     * @return the repository instance
     */
    protected abstract JpaRepository<T, ID> getRepository();

    /**
     * Creates a test entity for DDoS testing.
     * Must be implemented by concrete test classes.
     *
     * @return a new test entity
     */
    protected abstract T createTestEntity();

    /**
     * Creates a test entity with specific identifier.
     * Default implementation calls createTestEntity().
     *
     * @param identifier unique identifier for the entity
     * @return a new test entity with the specified identifier
     */
    protected T createTestEntity(String identifier) {
        return createTestEntity();
    }

    /**
     * Gets the ID from an entity.
     * Must be implemented by concrete test classes.
     *
     * @param entity the entity
     * @return the entity's ID
     */
    protected abstract ID getEntityId(T entity);

    /**
     * Updates an entity for testing purposes.
     * Default implementation returns the entity unchanged.
     *
     * @param entity the entity to update
     * @return the updated entity
     */
    protected T updateEntity(T entity) {
        return entity;
    }

    @Test
    @DisplayName("DDoS Test: Concurrent Read Operations")
    @Description("Tests repository behavior under concurrent read operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    public void testConcurrentReadOperations() {
        logger.info("[DEBUG_LOG] Starting concurrent read operations DDoS test");

        // Pre-populate with test data
        List<T> testEntities = createTestData(10);
        List<ID> entityIds = new ArrayList<>();
        for (T entity : testEntities) {
            T saved = getRepository().save(entity);
            entityIds.add(getEntityId(saved));
        }

        attachText("Test Data", "Created " + entityIds.size() + " test entities for concurrent read test");

        // Execute concurrent read operations
        Supplier<Void> readOperation = () -> {
            try {
                // Random read operation
                ID randomId = entityIds.get(ThreadLocalRandom.current().nextInt(entityIds.size()));
                Optional<T> result = getRepository().findById(randomId);

                // Verify result exists
                if (result.isEmpty()) {
                    throw new RuntimeException("Entity not found: " + randomId);
                }

                return null;
            } catch (Exception e) {
                logger.error("[DEBUG_LOG] Read operation failed: {}", e.getMessage());
                throw e;
            }
        };

        DdosTestResult result = executeDdosTest(readOperation);

        // Verify results
        assertDdosTestResults(result, "Concurrent Read Operations");

        logger.info("[DEBUG_LOG] Concurrent read operations DDoS test completed: {}", result);
    }

    @Test
    @DisplayName("DDoS Test: Concurrent Write Operations")
    @Description("Tests repository behavior under concurrent write operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    @Transactional
    public void testConcurrentWriteOperations() {
        logger.info("[DEBUG_LOG] Starting concurrent write operations DDoS test");

        // Execute concurrent write operations
        Supplier<Void> writeOperation = () -> {
            try {
                // Create and save new entity
                T entity = createTestEntity("ddos-" + UUID.randomUUID());
                T saved = getRepository().save(entity);

                // Verify entity was saved
                if (saved == null) {
                    throw new RuntimeException("Failed to save entity");
                }

                return null;
            } catch (Exception e) {
                logger.error("[DEBUG_LOG] Write operation failed: {}", e.getMessage());
                throw e;
            }
        };

        DdosTestResult result = executeDdosTest(writeOperation);

        // Verify results
        assertDdosTestResults(result, "Concurrent Write Operations");

        logger.info("[DEBUG_LOG] Concurrent write operations DDoS test completed: {}", result);
    }

    @Test
    @DisplayName("DDoS Test: Mixed Read/Write Operations")
    @Description("Tests repository behavior under mixed concurrent read/write operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    @Transactional
    public void testMixedReadWriteOperations() {
        logger.info("[DEBUG_LOG] Starting mixed read/write operations DDoS test");

        // Pre-populate with test data
        List<T> testEntities = createTestData(5);
        List<ID> entityIds = new ArrayList<>();
        for (T entity : testEntities) {
            T saved = getRepository().save(entity);
            entityIds.add(getEntityId(saved));
        }

        // Execute mixed read/write operations
        Supplier<Void> mixedOperation = () -> {
            try {
                boolean isReadOperation = ThreadLocalRandom.current().nextBoolean();

                if (isReadOperation && !entityIds.isEmpty()) {
                    // Read operation
                    ID randomId = entityIds.get(ThreadLocalRandom.current().nextInt(entityIds.size()));
                    Optional<T> result = getRepository().findById(randomId);

                    if (result.isEmpty()) {
                        throw new RuntimeException("Entity not found: " + randomId);
                    }
                } else {
                    // Write operation
                    T entity = createTestEntity("mixed-" + UUID.randomUUID());
                    T saved = getRepository().save(entity);

                    if (saved == null) {
                        throw new RuntimeException("Failed to save entity");
                    }

                    // Add to list for future reads (thread-safe)
                    synchronized (entityIds) {
                        entityIds.add(getEntityId(saved));
                    }
                }

                return null;
            } catch (Exception e) {
                logger.error("[DEBUG_LOG] Mixed operation failed: {}", e.getMessage());
                throw e;
            }
        };

        DdosTestResult result = executeDdosTest(mixedOperation);

        // Verify results
        assertDdosTestResults(result, "Mixed Read/Write Operations");

        logger.info("[DEBUG_LOG] Mixed read/write operations DDoS test completed: {}", result);
    }

    @Test
    @DisplayName("DDoS Test: Aggressive Repository Stress Test")
    @Description("Tests repository behavior under aggressive load simulating intense DDoS attack")
    @Severity(SeverityLevel.BLOCKER)
    @Transactional
    public void testAggressiveRepositoryStress() {
        logger.info("[DEBUG_LOG] Starting aggressive repository stress test");

        // Pre-populate with test data
        List<T> testEntities = createTestData(20);
        List<ID> entityIds = new ArrayList<>();
        for (T entity : testEntities) {
            T saved = getRepository().save(entity);
            entityIds.add(getEntityId(saved));
        }

        // Execute aggressive mixed operations
        Supplier<Void> aggressiveOperation = () -> {
            try {
                int operationType = ThreadLocalRandom.current().nextInt(4);

                switch (operationType) {
                    case 0: // Read by ID
                        if (!entityIds.isEmpty()) {
                            ID randomId = entityIds.get(ThreadLocalRandom.current().nextInt(entityIds.size()));
                            getRepository().findById(randomId);
                        }
                        break;

                    case 1: // Create new entity
                        T entity = createTestEntity("aggressive-" + UUID.randomUUID());
                        T saved = getRepository().save(entity);
                        synchronized (entityIds) {
                            entityIds.add(getEntityId(saved));
                        }
                        break;

                    case 2: // Update existing entity
                        if (!entityIds.isEmpty()) {
                            ID randomId = entityIds.get(ThreadLocalRandom.current().nextInt(entityIds.size()));
                            Optional<T> existing = getRepository().findById(randomId);
                            if (existing.isPresent()) {
                                T updated = updateEntity(existing.get());
                                getRepository().save(updated);
                            }
                        }
                        break;

                    case 3: // Count all entities
                        getRepository().count();
                        break;
                }

                return null;
            } catch (Exception e) {
                logger.error("[DEBUG_LOG] Aggressive operation failed: {}", e.getMessage());
                throw e;
            }
        };

        DdosTestResult result = executeAggressiveDdosTest(aggressiveOperation);

        // Verify results (more lenient for aggressive tests)
        assertAggressiveDdosTestResults(result, "Aggressive Repository Stress Test");

        logger.info("[DEBUG_LOG] Aggressive repository stress test completed: {}", result);
    }

    @Test
    @DisplayName("DDoS Test: Repository Bulk Operations")
    @Description("Tests repository behavior under bulk operations simulating DDoS attack")
    @Severity(SeverityLevel.CRITICAL)
    @Transactional
    public void testBulkOperations() {
        logger.info("[DEBUG_LOG] Starting bulk operations DDoS test");

        // Execute bulk operations
        Supplier<Void> bulkOperation = () -> {
            try {
                // Create multiple entities in one operation
                List<T> entities = createTestData(5);
                List<T> saved = getRepository().saveAll(entities);

                if (saved.size() != entities.size()) {
                    throw new RuntimeException("Bulk save failed: expected " + entities.size() + ", got " + saved.size());
                }

                return null;
            } catch (Exception e) {
                logger.error("[DEBUG_LOG] Bulk operation failed: {}", e.getMessage());
                throw e;
            }
        };

        DdosTestResult result = executeDdosTest(bulkOperation, 20, 10); // Reduced load for bulk operations

        // Verify results
        assertDdosTestResults(result, "Bulk Operations");

        logger.info("[DEBUG_LOG] Bulk operations DDoS test completed: {}", result);
    }

    /**
     * Creates test data for DDoS testing.
     *
     * @param count number of test entities to create
     * @return list of test entities
     */
    protected List<T> createTestData(int count) {
        List<T> entities = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            entities.add(createTestEntity("test-data-" + i));
        }
        return entities;
    }

    /**
     * Asserts DDoS test results meet minimum requirements.
     *
     * @param result   the test result
     * @param testName the name of the test
     */
    @Step("Asserting DDoS test results for {testName}")
    protected void assertDdosTestResults(DdosTestResult result, String testName) {
        logger.info("[DEBUG_LOG] Asserting DDoS test results for {}: {}", testName, result);

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
    @Step("Asserting aggressive DDoS test results for {testName}")
    protected void assertAggressiveDdosTestResults(DdosTestResult result, String testName) {
        logger.info("[DEBUG_LOG] Asserting aggressive DDoS test results for {}: {}", testName, result);

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
}
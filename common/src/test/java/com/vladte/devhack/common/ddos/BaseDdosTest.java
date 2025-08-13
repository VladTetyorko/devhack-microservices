package com.vladte.devhack.common.ddos;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Base class for DDoS simulation tests.
 * Provides common functionality for testing services and repositories under high load conditions.
 * <p>
 * This class simulates DDoS attacks by creating multiple concurrent threads that execute
 * operations simultaneously to test system behavior under stress.
 */
@ActiveProfiles("test")
public abstract class BaseDdosTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // DDoS simulation configuration
    protected static final int DEFAULT_THREAD_COUNT = 50;
    protected static final int DEFAULT_REQUESTS_PER_THREAD = 100;
    protected static final int DEFAULT_TIMEOUT_SECONDS = 30;
    protected static final int AGGRESSIVE_THREAD_COUNT = 100;
    protected static final int AGGRESSIVE_REQUESTS_PER_THREAD = 200;

    // Metrics tracking
    protected final AtomicInteger successfulRequests = new AtomicInteger(0);
    protected final AtomicInteger failedRequests = new AtomicInteger(0);
    protected final AtomicLong totalExecutionTime = new AtomicLong(0);
    protected final List<Exception> exceptions = new ArrayList<>();

    @BeforeEach
    public void setUp(TestInfo testInfo) {
        logTestStart(testInfo.getDisplayName());
        resetMetrics();
    }

    @AfterEach
    public void tearDown(TestInfo testInfo) {
        logTestEnd(testInfo.getDisplayName());
        attachDdosMetrics();
    }

    /**
     * Resets all metrics before each test.
     */
    protected void resetMetrics() {
        successfulRequests.set(0);
        failedRequests.set(0);
        totalExecutionTime.set(0);
        exceptions.clear();
    }

    /**
     * Logs the start of a DDoS test with Allure reporting.
     *
     * @param testName the name of the test
     */
    @Step("Starting DDoS test: {testName}")
    protected void logTestStart(String testName) {
        logger.info("[DEBUG_LOG] Starting DDoS test: {}", testName);
    }

    /**
     * Logs the end of a DDoS test with Allure reporting.
     *
     * @param testName the name of the test
     */
    @Step("Finished DDoS test: {testName}")
    protected void logTestEnd(String testName) {
        logger.info("[DEBUG_LOG] Finished DDoS test: {}", testName);
    }

    /**
     * Executes a DDoS simulation with default parameters.
     *
     * @param operation the operation to execute under load
     * @return DDoS test results
     */
    protected DdosTestResult executeDdosTest(Supplier<Void> operation) {
        return executeDdosTest(operation, DEFAULT_THREAD_COUNT, DEFAULT_REQUESTS_PER_THREAD);
    }

    /**
     * Executes a DDoS simulation with custom parameters.
     *
     * @param operation         the operation to execute under load
     * @param threadCount       number of concurrent threads
     * @param requestsPerThread number of requests per thread
     * @return DDoS test results
     */
    @Step("Executing DDoS test with {threadCount} threads and {requestsPerThread} requests per thread")
    protected DdosTestResult executeDdosTest(Supplier<Void> operation, int threadCount, int requestsPerThread) {
        logger.info("[DEBUG_LOG] Starting DDoS simulation: {} threads, {} requests per thread", threadCount, requestsPerThread);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        long startTime = System.currentTimeMillis();

        // Create and submit tasks
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // Wait for all threads to be ready
                    startLatch.await();

                    // Execute requests for this thread
                    for (int j = 0; j < requestsPerThread; j++) {
                        executeRequest(operation, threadId, j);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("[DEBUG_LOG] Thread {} interrupted", threadId, e);
                } finally {
                    endLatch.countDown();
                }
            }, executor);
            futures.add(future);
        }

        // Start all threads simultaneously
        startLatch.countDown();

        try {
            // Wait for all threads to complete or timeout
            boolean completed = endLatch.await(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                logger.warn("[DEBUG_LOG] DDoS test timed out after {} seconds", DEFAULT_TIMEOUT_SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("[DEBUG_LOG] DDoS test interrupted", e);
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // Shutdown executor
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Create and return results
        DdosTestResult result = new DdosTestResult(
                threadCount,
                requestsPerThread,
                successfulRequests.get(),
                failedRequests.get(),
                totalTime,
                new ArrayList<>(exceptions)
        );

        logger.info("[DEBUG_LOG] DDoS test completed: {}", result);
        return result;
    }

    /**
     * Executes a single request and tracks metrics.
     *
     * @param operation the operation to execute
     * @param threadId  the thread identifier
     * @param requestId the request identifier within the thread
     */
    private void executeRequest(Supplier<Void> operation, int threadId, int requestId) {
        long requestStart = System.currentTimeMillis();
        try {
            operation.get();
            successfulRequests.incrementAndGet();
        } catch (Exception e) {
            failedRequests.incrementAndGet();
            synchronized (exceptions) {
                exceptions.add(e);
            }
            logger.debug("[DEBUG_LOG] Request failed in thread {} request {}: {}", threadId, requestId, e.getMessage());
        } finally {
            long requestTime = System.currentTimeMillis() - requestStart;
            totalExecutionTime.addAndGet(requestTime);
        }
    }

    /**
     * Executes an aggressive DDoS test with higher load.
     *
     * @param operation the operation to execute under load
     * @return DDoS test results
     */
    @Step("Executing aggressive DDoS test")
    protected DdosTestResult executeAggressiveDdosTest(Supplier<Void> operation) {
        return executeDdosTest(operation, AGGRESSIVE_THREAD_COUNT, AGGRESSIVE_REQUESTS_PER_THREAD);
    }

    /**
     * Attaches DDoS metrics to the Allure report.
     */
    @Attachment(value = "DDoS Test Metrics", type = "text/plain")
    protected String attachDdosMetrics() {
        StringBuilder metrics = new StringBuilder();
        metrics.append("DDoS Test Metrics\n");
        metrics.append("=================\n");
        metrics.append("Successful Requests: ").append(successfulRequests.get()).append("\n");
        metrics.append("Failed Requests: ").append(failedRequests.get()).append("\n");
        metrics.append("Total Requests: ").append(successfulRequests.get() + failedRequests.get()).append("\n");
        metrics.append("Success Rate: ").append(calculateSuccessRate()).append("%\n");
        metrics.append("Total Execution Time: ").append(totalExecutionTime.get()).append(" ms\n");
        metrics.append("Average Request Time: ").append(calculateAverageRequestTime()).append(" ms\n");
        metrics.append("Exceptions Count: ").append(exceptions.size()).append("\n");

        if (!exceptions.isEmpty()) {
            metrics.append("\nException Details:\n");
            exceptions.stream()
                    .limit(10) // Limit to first 10 exceptions to avoid huge reports
                    .forEach(e -> metrics.append("- ").append(e.getClass().getSimpleName())
                            .append(": ").append(e.getMessage()).append("\n"));
        }

        String metricsString = metrics.toString();
        logger.info("[DEBUG_LOG] DDoS Metrics: {}", metricsString);
        return metricsString;
    }

    /**
     * Calculates the success rate percentage.
     *
     * @return success rate as percentage
     */
    protected double calculateSuccessRate() {
        int total = successfulRequests.get() + failedRequests.get();
        return total > 0 ? (double) successfulRequests.get() / total * 100 : 0;
    }

    /**
     * Calculates the average request execution time.
     *
     * @return average request time in milliseconds
     */
    protected double calculateAverageRequestTime() {
        int total = successfulRequests.get() + failedRequests.get();
        return total > 0 ? (double) totalExecutionTime.get() / total : 0;
    }

    /**
     * Attaches text content to the Allure report.
     *
     * @param name    the name of the attachment
     * @param content the content to attach
     */
    protected void attachText(String name, String content) {
        Allure.addAttachment(name, "text/plain",
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), ".txt");
    }

    /**
     * Attaches JSON content to the Allure report.
     *
     * @param name the name of the attachment
     * @param json the JSON content to attach
     */
    protected void attachJson(String name, String json) {
        Allure.addAttachment(name, "application/json",
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), ".json");
    }

    /**
     * Data class to hold DDoS test results.
     */
    public static class DdosTestResult {
        private final int threadCount;
        private final int requestsPerThread;
        private final int successfulRequests;
        private final int failedRequests;
        private final long totalExecutionTime;
        private final List<Exception> exceptions;

        public DdosTestResult(int threadCount, int requestsPerThread, int successfulRequests,
                              int failedRequests, long totalExecutionTime, List<Exception> exceptions) {
            this.threadCount = threadCount;
            this.requestsPerThread = requestsPerThread;
            this.successfulRequests = successfulRequests;
            this.failedRequests = failedRequests;
            this.totalExecutionTime = totalExecutionTime;
            this.exceptions = exceptions;
        }

        // Getters
        public int getThreadCount() {
            return threadCount;
        }

        public int getRequestsPerThread() {
            return requestsPerThread;
        }

        public int getSuccessfulRequests() {
            return successfulRequests;
        }

        public int getFailedRequests() {
            return failedRequests;
        }

        public int getTotalRequests() {
            return successfulRequests + failedRequests;
        }

        public long getTotalExecutionTime() {
            return totalExecutionTime;
        }

        public List<Exception> getExceptions() {
            return exceptions;
        }

        public double getSuccessRate() {
            return getTotalRequests() > 0 ? (double) successfulRequests / getTotalRequests() * 100 : 0;
        }

        public double getAverageRequestTime() {
            return getTotalRequests() > 0 ? (double) totalExecutionTime / getTotalRequests() : 0;
        }

        @Override
        public String toString() {
            return String.format(
                    "DdosTestResult{threads=%d, requestsPerThread=%d, successful=%d, failed=%d, " +
                            "successRate=%.2f%%, avgTime=%.2fms, totalTime=%dms}",
                    threadCount, requestsPerThread, successfulRequests, failedRequests,
                    getSuccessRate(), getAverageRequestTime(), totalExecutionTime
            );
        }
    }
}
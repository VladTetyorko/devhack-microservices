package com.vladte.devhack.common;

import com.vladte.devhack.common.service.BaseServiceTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Demonstration test class showing comprehensive Allure reporting features.
 * This class demonstrates proper use of @Attachment, tags, descriptions, and coverage reporting.
 */
@Epic("Test Framework")
@Feature("Allure Reporting")
@DisplayName("Allure Test Demonstration")
@Tag("allure")
@Tag("demonstration")
@Tag("coverage")
class AllureTestDemonstration extends BaseServiceTest {

    @Test
    @Story("Basic Test Logging")
    @DisplayName("Should demonstrate basic test logging with attachments")
    @Description("This test demonstrates how to use @Attachment annotations for comprehensive test logging")
    @Severity(SeverityLevel.NORMAL)
    @Tag("logging")
    @Tag("attachment")
    void testBasicLoggingWithAttachments() {
        attachTestLog("Starting basic logging demonstration");

        // Given
        String testData = "Sample test data for demonstration";
        attachTestData(testData);

        // When
        attachTestStep("Processing", "Processing the test data");
        String result = processTestData(testData);

        // Then
        attachTestStep("Verification", "Verifying the processed result");
        assertNotNull(result);
        assertEquals("PROCESSED: " + testData, result);

        attachMethodExecution("processTestData", testData, result);
        attachTestLog("Basic logging demonstration completed successfully");
    }

    @Test
    @Story("Error Handling")
    @DisplayName("Should demonstrate error logging with attachments")
    @Description("This test demonstrates how to log errors and exceptions using @Attachment")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("error-handling")
    @Tag("exception")
    void testErrorLoggingWithAttachments() {
        attachTestLog("Starting error logging demonstration");

        try {
            // Given
            String invalidData = null;
            attachTestData(invalidData);

            // When
            attachTestStep("Error Simulation", "Attempting to process null data");
            processTestData(invalidData);

            fail("Expected exception was not thrown");
        } catch (IllegalArgumentException e) {
            // Then
            attachError("Expected exception caught", e);
            attachTestStep("Exception Handling", "Successfully caught and logged exception");
            assertEquals("Data cannot be null", e.getMessage());
        }

        attachTestLog("Error logging demonstration completed successfully");
    }

    @Test
    @Story("Performance Testing")
    @DisplayName("Should demonstrate performance logging with attachments")
    @Description("This test demonstrates how to log performance metrics using @Attachment")
    @Severity(SeverityLevel.MINOR)
    @Tag("performance")
    @Tag("metrics")
    void testPerformanceLoggingWithAttachments() {
        attachTestLog("Starting performance logging demonstration");

        // Given
        int iterations = 1000;
        attachTestData("Iterations: " + iterations);

        // When
        attachTestStep("Performance Test", "Running performance test with " + iterations + " iterations");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            processTestData("Test data " + i);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then
        attachPerformanceMetrics("Performance Test Results", iterations, duration);
        attachTestStep("Performance Verification", "Verifying performance is within acceptable limits");
        assertTrue(duration < 5000, "Performance test should complete within 5 seconds");

        attachTestLog("Performance logging demonstration completed successfully");
    }

    @Test
    @Story("Data Validation")
    @DisplayName("Should demonstrate comprehensive data validation with attachments")
    @Description("This test demonstrates comprehensive data validation logging using multiple @Attachment types")
    @Severity(SeverityLevel.BLOCKER)
    @Tag("validation")
    @Tag("data-integrity")
    void testComprehensiveDataValidationWithAttachments() {
        attachTestLog("Starting comprehensive data validation demonstration");

        // Given
        TestDataModel testModel = createTestDataModel();
        attachTestDataModel(testModel);

        // When
        attachTestStep("Validation", "Validating test data model");
        ValidationResult result = validateTestDataModel(testModel);

        // Then
        attachValidationResult(result);
        attachTestStep("Result Verification", "Verifying validation results");

        assertTrue(result.isValid(), "Test data model should be valid");
        assertEquals(0, result.getErrorCount(), "Should have no validation errors");
        assertNotNull(result.getValidatedFields(), "Validated fields should not be null");

        attachTestLog("Comprehensive data validation demonstration completed successfully");
    }

    @Test
    @Story("Integration Testing")
    @DisplayName("Should demonstrate integration test logging with attachments")
    @Description("This test demonstrates how to log integration test scenarios using @Attachment")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("integration")
    @Tag("workflow")
    void testIntegrationWorkflowWithAttachments() {
        attachTestLog("Starting integration workflow demonstration");

        // Given
        WorkflowContext context = new WorkflowContext();
        attachWorkflowContext("Initial Context", context);

        // When - Step 1
        attachTestStep("Workflow Step 1", "Initializing workflow");
        context.initialize("test-workflow");
        attachWorkflowContext("After Initialization", context);

        // When - Step 2
        attachTestStep("Workflow Step 2", "Processing workflow data");
        context.processData("sample-data");
        attachWorkflowContext("After Processing", context);

        // When - Step 3
        attachTestStep("Workflow Step 3", "Finalizing workflow");
        WorkflowResult result = context.complete();
        attachWorkflowResult(result);

        // Then
        attachTestStep("Integration Verification", "Verifying complete workflow execution");
        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals("test-workflow", result.getWorkflowId());

        attachTestLog("Integration workflow demonstration completed successfully");
    }

    // Helper methods for demonstration

    private String processTestData(String data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        return "PROCESSED: " + data;
    }

    private TestDataModel createTestDataModel() {
        return new TestDataModel("test-id", "test-name", "test-value");
    }

    private ValidationResult validateTestDataModel(TestDataModel model) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);
        result.setErrorCount(0);
        result.setValidatedFields(new String[]{"id", "name", "value"});
        return result;
    }

    // @Attachment methods for comprehensive logging

    @Attachment(value = "Performance Metrics", type = "text/plain")
    private String attachPerformanceMetrics(String testName, int iterations, long duration) {
        String metrics = String.format(
                "Performance Test: %s\nIterations: %d\nDuration: %d ms\nAverage per iteration: %.2f ms",
                testName, iterations, duration, (double) duration / iterations
        );
        logger.info("[DEBUG_LOG] Performance Metrics: {}", metrics);
        return metrics;
    }

    @Attachment(value = "Test Data Model", type = "application/json")
    private String attachTestDataModel(TestDataModel model) {
        String modelJson = String.format(
                "{\n  \"id\": \"%s\",\n  \"name\": \"%s\",\n  \"value\": \"%s\"\n}",
                model.getId(), model.getName(), model.getValue()
        );
        logger.debug("[DEBUG_LOG] Test Data Model: {}", modelJson);
        return modelJson;
    }

    @Attachment(value = "Validation Result", type = "text/plain")
    private String attachValidationResult(ValidationResult result) {
        String resultInfo = String.format(
                "Validation Result:\nValid: %s\nError Count: %d\nValidated Fields: %s",
                result.isValid(), result.getErrorCount(), String.join(", ", result.getValidatedFields())
        );
        logger.info("[DEBUG_LOG] Validation Result: {}", resultInfo);
        return resultInfo;
    }

    @Attachment(value = "Workflow Context", type = "application/json")
    private String attachWorkflowContext(String stage, WorkflowContext context) {
        String contextJson = String.format(
                "{\n  \"stage\": \"%s\",\n  \"workflowId\": \"%s\",\n  \"status\": \"%s\",\n  \"data\": \"%s\"\n}",
                stage, context.getWorkflowId(), context.getStatus(), context.getData()
        );
        logger.debug("[DEBUG_LOG] Workflow Context [{}]: {}", stage, contextJson);
        return contextJson;
    }

    @Attachment(value = "Workflow Result", type = "application/json")
    private String attachWorkflowResult(WorkflowResult result) {
        String resultJson = String.format(
                "{\n  \"workflowId\": \"%s\",\n  \"successful\": %s,\n  \"message\": \"%s\"\n}",
                result.getWorkflowId(), result.isSuccessful(), result.getMessage()
        );
        logger.info("[DEBUG_LOG] Workflow Result: {}", resultJson);
        return resultJson;
    }

    // Test data classes for demonstration

    private static class TestDataModel {
        private final String id;
        private final String name;
        private final String value;

        public TestDataModel(String id, String name, String value) {
            this.id = id;
            this.name = name;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    private static class ValidationResult {
        private boolean valid;
        private int errorCount;
        private String[] validatedFields;

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public void setErrorCount(int errorCount) {
            this.errorCount = errorCount;
        }

        public String[] getValidatedFields() {
            return validatedFields;
        }

        public void setValidatedFields(String[] validatedFields) {
            this.validatedFields = validatedFields;
        }
    }

    private static class WorkflowContext {
        private String workflowId;
        private String status = "CREATED";
        private String data;

        public void initialize(String id) {
            this.workflowId = id;
            this.status = "INITIALIZED";
        }

        public void processData(String data) {
            this.data = data;
            this.status = "PROCESSING";
        }

        public WorkflowResult complete() {
            this.status = "COMPLETED";
            return new WorkflowResult(workflowId, true, "Workflow completed successfully");
        }

        public String getWorkflowId() {
            return workflowId;
        }

        public String getStatus() {
            return status;
        }

        public String getData() {
            return data;
        }
    }

    private static class WorkflowResult {
        private final String workflowId;
        private final boolean successful;
        private final String message;

        public WorkflowResult(String workflowId, boolean successful, String message) {
            this.workflowId = workflowId;
            this.successful = successful;
            this.message = message;
        }

        public String getWorkflowId() {
            return workflowId;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public String getMessage() {
            return message;
        }
    }
}

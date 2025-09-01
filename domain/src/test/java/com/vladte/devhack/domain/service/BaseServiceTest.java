package com.vladte.devhack.domain.service;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base class for all service unit tests.
 * Provides common functionality and configurations for testing services.
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public abstract class BaseServiceTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @BeforeEach
    public void setUp(TestInfo testInfo) {
        logTestStart(testInfo.getDisplayName());
    }

    @AfterEach
    public void tearDown(TestInfo testInfo) {
        logTestEnd(testInfo.getDisplayName());
    }

    /**
     * Logs the start of a test with Allure reporting.
     *
     * @param testName the name of the test
     */
    @Step("Starting test: {testName}")
    protected void logTestStart(String testName) {
        logger.info("Starting test: {}", testName);
    }

    /**
     * Logs the end of a test with Allure reporting.
     *
     * @param testName the name of the test
     */
    @Step("Finished test: {testName}")
    protected void logTestEnd(String testName) {
        logger.info("Finished test: {}", testName);
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
     * Attaches test execution log to the Allure report.
     *
     * @param message the log message
     * @return the formatted log message with timestamp
     */
    @Attachment(value = "Test Execution Log", type = "text/plain")
    protected String attachTestLog(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        String logEntry = String.format("[%s] %s", timestamp, message);
        logger.debug("[DEBUG_LOG] {}", logEntry);
        return logEntry;
    }

    /**
     * Attaches test data to the Allure report.
     *
     * @param data the test data to attach
     * @return the test data as string
     */
    @Attachment(value = "Test Data", type = "application/json")
    protected String attachTestData(Object data) {
        String dataString = data != null ? data.toString() : "null";
        logger.debug("[DEBUG_LOG] Test Data: {}", dataString);
        return dataString;
    }

    /**
     * Attaches error information to the Allure report.
     *
     * @param error     the error message
     * @param exception the exception (can be null)
     * @return the formatted error information
     */
    @Attachment(value = "Error Information", type = "text/plain")
    protected String attachError(String error, Throwable exception) {
        StringBuilder errorInfo = new StringBuilder();
        errorInfo.append("Error: ").append(error).append("\n");
        if (exception != null) {
            errorInfo.append("Exception: ").append(exception.getClass().getSimpleName()).append("\n");
            errorInfo.append("Message: ").append(exception.getMessage()).append("\n");
            errorInfo.append("Stack Trace:\n");
            for (StackTraceElement element : exception.getStackTrace()) {
                errorInfo.append("  at ").append(element.toString()).append("\n");
            }
        }
        String errorString = errorInfo.toString();
        logger.error("[DEBUG_LOG] {}", errorString);
        return errorString;
    }

    /**
     * Attaches test step information to the Allure report.
     *
     * @param stepName the name of the test step
     * @param stepData the data associated with the step
     * @return the formatted step information
     */
    @Attachment(value = "Test Step", type = "text/plain")
    protected String attachTestStep(String stepName, String stepData) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String stepInfo = String.format("[%s] Step: %s\nData: %s", timestamp, stepName, stepData);
        logger.info("[DEBUG_LOG] {}", stepInfo);
        return stepInfo;
    }

    /**
     * Attaches method execution details to the Allure report.
     *
     * @param methodName the name of the method being tested
     * @param parameters the method parameters
     * @param result     the method result
     * @return the formatted method execution details
     */
    @Attachment(value = "Method Execution", type = "text/plain")
    protected String attachMethodExecution(String methodName, String parameters, String result) {
        String executionInfo = String.format(
                "Method: %s\nParameters: %s\nResult: %s\nTimestamp: %s",
                methodName,
                parameters,
                result,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        );
        logger.info("[DEBUG_LOG] Method Execution: {}", executionInfo);
        return executionInfo;
    }
}

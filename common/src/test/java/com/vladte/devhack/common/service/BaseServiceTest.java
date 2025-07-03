package com.vladte.devhack.common.service;

import io.qameta.allure.Allure;
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
}
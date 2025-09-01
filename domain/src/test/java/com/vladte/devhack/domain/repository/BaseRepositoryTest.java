package com.vladte.devhack.domain.repository;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Base class for all repository unit tests.
 * Provides common functionality and configurations for testing repositories.
 */
@DataJpaTest
@EnableJpaAuditing
@ActiveProfiles("test")
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
@org.testcontainers.junit.jupiter.Testcontainers
public abstract class BaseRepositoryTest {

    @Container
    static PostgreSQLContainer<?> pg = new org.testcontainers.containers.PostgreSQLContainer<>("postgres:16");

    @org.springframework.test.context.DynamicPropertySource
    static void db(org.springframework.test.context.DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        r.add("spring.liquibase.enabled", () -> true);
        r.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.yaml");
    }

    protected final Logger log = LoggerFactory.getLogger(getClass());

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
    @Step("Starting repository test: {testName}")
    protected void logTestStart(String testName) {
        log.info("Starting repository test: {}", testName);
    }

    /**
     * Logs the end of a test with Allure reporting.
     *
     * @param testName the name of the test
     */
    @Step("Finished repository test: {testName}")
    protected void logTestEnd(String testName) {
        log.info("Finished repository test: {}", testName);
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

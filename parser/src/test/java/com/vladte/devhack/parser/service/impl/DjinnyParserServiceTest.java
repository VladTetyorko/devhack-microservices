package com.vladte.devhack.parser.service.impl;

import com.vladte.devhack.entities.global.Vacancy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the DjinnyParserService.
 */
@ExtendWith(MockitoExtension.class)
class DjinnyParserServiceTest {

    @InjectMocks
    private DjinnyParserService parserService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void parse_emptyHtml_returnsEmptyList() {
        // Given
        String htmlContent = "";

        // When
        List<Vacancy> result = parserService.parse(htmlContent);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void parse_invalidHtml_returnsEmptyList() {
        // Given
        String htmlContent = "<invalid>html</invalid>";

        // When
        List<Vacancy> result = parserService.parse(htmlContent);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void parse_validHtmlWithNoJobs_returnsEmptyList() {
        // Given
        String htmlContent = "<html><body><div class='container'></div></body></html>";

        // When
        List<Vacancy> result = parserService.parse(htmlContent);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void parse_validHtmlWithJobs_returnsVacancies() {
        // Given
        String htmlContent = """
                <html>
                <body>
                <div class='job-list-item'>
                    <div class='company-name'>Test Company</div>
                    <div class='job-title'>Java Developer</div>
                    <div class='job-tags'>Java, Spring, Hibernate</div>
                    <a class='job-link' href='/jobs/123'>View Job</a>
                    <div class='remote-tag'>Remote</div>
                </div>
                </body>
                </html>
                """;

        // When
        List<Vacancy> result = parserService.parse(htmlContent);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());

        Vacancy vacancy = result.get(0);
        assertEquals("Test Company", vacancy.getCompanyName());
        assertEquals("Java Developer", vacancy.getPosition());
        assertEquals("Java, Spring, Hibernate", vacancy.getTechnologies());
        assertEquals("https://djinny.co/jobs/123", vacancy.getUrl());
        assertTrue(vacancy.getRemoteAllowed());
        assertEquals("Djinny", vacancy.getSource());
    }
}
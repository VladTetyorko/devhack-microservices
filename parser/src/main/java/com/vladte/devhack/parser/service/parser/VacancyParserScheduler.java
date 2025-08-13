package com.vladte.devhack.parser.service.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Scheduler for periodically parsing job vacancies from various sources.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VacancyParserScheduler {

    private final VacancyParserService vacancyService;
    private final RestTemplate restTemplate;

    @Value("${vacancy.parser.enabled:true}")
    private boolean parserEnabled;

    // URLs for job websites
    private final Map<String, String> sourceUrls = Map.of(
            "djinny", "https://djinny.co/jobs/",
            "dou", "https://jobs.dou.ua/vacancies/",
            "linkedin", "https://www.linkedin.com/jobs/search/"
    );

    /**
     * Scheduled task to parse job vacancies from all sources.
     * Runs every hour by default.
     */
    @Scheduled(cron = "${vacancy.parser.cron:0 0 * * * ?}")
    public void parseAllSources() {
        if (!parserEnabled) {
            log.info("Vacancy parser is disabled");
            return;
        }

        log.info("Starting scheduled parsing of job vacancies from all sources");

        sourceUrls.forEach(this::parseSource);

        log.info("Completed scheduled parsing of job vacancies from all sources");
    }

    /**
     * Parse job vacancies from a specific source.
     *
     * @param source The source to parse
     * @param url    The URL to fetch HTML content from
     */
    private void parseSource(String source, String url) {
        log.info("Parsing job vacancies from source: {} ({})", source, url);

        try {
            String htmlContent = fetchHtmlContent(url);
            if (htmlContent == null || htmlContent.isEmpty()) {
                log.error("Failed to fetch HTML content from URL: {}", url);
                return;
            }

            int savedCount = vacancyService.parseAndSave(source, htmlContent);
            log.info("Saved {} new vacancies from source: {}", savedCount, source);
        } catch (Exception e) {
            log.error("Error parsing job vacancies from source: {} - {}", source, e.getMessage());
        }
    }

    /**
     * Fetch HTML content from a URL.
     *
     * @param url The URL to fetch HTML content from
     * @return The HTML content
     */
    private String fetchHtmlContent(String url) {
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.error("Error fetching HTML content from URL: {} - {}", url, e.getMessage());
            return null;
        }
    }
}
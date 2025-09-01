package com.vladte.devhack.parser.service.parser;

import com.vladte.devhack.parser.entities.QueryParameters;
import com.vladte.devhack.parser.service.provider.VacancyScrappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Scheduler for periodically parsing job vacancies from various sources.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VacancyScrappingScheduler {

    private final VacancyScrappingService scrappingService;

    @Value("${vacancy.parser.enabled:true}")
    private boolean parserEnabled;

    private final Map<String, String> providers = Map.of(
            "djinni", "https://djinni.co/jobs/",
            "dou", "https://jobs.dou.ua/vacancies/",
            "linkedin", "https://www.linkedin.com/jobs/search/"
    );

    /**
     * Trigger an immediate parsing run once the application is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application ready: triggering initial vacancy parsing run");
        parseAllSources();
    }

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

        providers.forEach(this::parseSource);

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
            QueryParameters params = new QueryParameters();
            params.setUrl(url);
            params.setMaxClicks(3);
            params.setTopic("Java");

            int savedCount = scrappingService.scrapVacancies(source, params).size();
            log.info("Saved {} new vacancies from source: {}", savedCount, source);
        } catch (Exception e) {
            log.error("Error parsing job vacancies from source: {} - {}", source, e.getMessage());
        }
    }
}
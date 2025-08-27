package com.vladte.devhack.parser.service.provider;

import com.vladte.devhack.entities.global.Vacancy;
import com.vladte.devhack.parser.entities.QueryParameters;

import java.util.List;

/**
 * Generic provider scraper contract used by the scheduler/factory.
 */
public interface ProviderScraper {
    /**
     * Scrape new vacancies for the provider and persist them.
     * Returns the list of saved vacancies (new ones only).
     */
    List<Vacancy> scrapeAndSave(QueryParameters queryParameters);

    /**
     * @return provider name identifier stored as Vacancy.source
     */
    String getProviderName();
}

package com.vladte.devhack.parser.service.provider;

import com.vladte.devhack.entities.global.Vacancy;
import com.vladte.devhack.parser.entities.QueryParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class VacancyScrappingService {

    private final VacancyScraperFactory factory;

    public VacancyScrappingService(VacancyScraperFactory factory) {
        this.factory = factory;
    }

    /**
     * Delegate scraping to provider-specific scraper.
     */
    @Transactional
    public List<Vacancy> scrapVacancies(String providerName, QueryParameters queryParams) {
        ProviderScraper scraper = factory.get(providerName);
        return scraper.scrapeAndSave(queryParams);
    }
}

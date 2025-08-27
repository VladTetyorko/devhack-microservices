package com.vladte.devhack.parser.service.provider;

import com.vladte.devhack.parser.service.provider.impl.DjinniVacancyScraper;
import com.vladte.devhack.parser.service.provider.impl.DouVacancyScraper;
import com.vladte.devhack.parser.service.provider.impl.LinkedInVacancyScraper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VacancyScraperFactory {

    private final Map<String, ProviderScraper> scrapersByName;

    public VacancyScraperFactory(DouVacancyScraper dou,
                                 DjinniVacancyScraper djinni,
                                 LinkedInVacancyScraper linkedin) {
        this.scrapersByName = Map.of(
                dou.getProviderName(), dou,
                djinni.getProviderName(), djinni,
                linkedin.getProviderName(), linkedin
        );
    }

    public ProviderScraper get(String provider) {
        ProviderScraper scraper = scrapersByName.get(provider.toLowerCase());
        if (scraper == null) {
            throw new IllegalArgumentException("No scraper for provider=" + provider);
        }
        return scraper;
    }
}

package com.vladte.devhack.parser.service.provider.impl;

import com.vladte.devhack.entities.global.Vacancy;
import com.vladte.devhack.parser.entities.QueryParameters;
import com.vladte.devhack.parser.repository.VacancyRepository;
import com.vladte.devhack.parser.service.provider.AbstractVacancyScraper;
import com.vladte.devhack.parser.service.selenium.djinni.DjinniVacancyListPageLoader;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DjinniVacancyScraper extends AbstractVacancyScraper {

    public static final String PROVIDER = "djinni";

    private final DjinniVacancyListPageLoader listLoader;

    public DjinniVacancyScraper(VacancyRepository vacancyRepository, DjinniVacancyListPageLoader listLoader, WebDriver webDriver) {
        super(vacancyRepository, webDriver);
        this.listLoader = listLoader;
    }

    @Override
    public String getProviderName() {
        return PROVIDER;
    }

    @Override
    public List<Vacancy> scrapeAndSave(QueryParameters queryParameters) {
        Optional<Vacancy> last = getLastSaved(PROVIDER);
        String lastUrl = last.map(Vacancy::getUrl).orElse(null);

        // Mock loader returns list of simple entries with url/title/company; we'll just convert and stop at lastUrl
        List<DjinniVacancyListPageLoader.Item> items = listLoader.fetchVacancies(queryParameters);
        List<Vacancy> toPersist = new ArrayList<>();
        for (DjinniVacancyListPageLoader.Item it : items) {
            if (lastUrl != null && lastUrl.equals(it.url())) break;
            Vacancy v = buildVacancy(PROVIDER, it.company(), it.title(), it.url(), LocalDateTime.now());
            toPersist.add(v);
        }
        if (toPersist.isEmpty()) return List.of();
        List<Vacancy> saved = saveAll(toPersist);
        log.info("[DJINNI] Saved {} new vacancies", saved.size());
        return saved;
    }
}

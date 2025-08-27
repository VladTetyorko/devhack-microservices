package com.vladte.devhack.parser.service.provider.impl;

import com.vladte.devhack.entities.enums.VacancyStatus;
import com.vladte.devhack.entities.global.Vacancy;
import com.vladte.devhack.parser.entities.QueryParameters;
import com.vladte.devhack.parser.entities.items.DouVacancyItem;
import com.vladte.devhack.parser.repository.VacancyRepository;
import com.vladte.devhack.parser.service.provider.AbstractVacancyScraper;
import com.vladte.devhack.parser.service.selenium.dou.DouVacancyDetailsPageLoader;
import com.vladte.devhack.parser.service.selenium.dou.DouVacancyListPageLoader;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DouVacancyScraper extends AbstractVacancyScraper {

    public static final String PROVIDER = "dou";

    public DouVacancyScraper(VacancyRepository vacancyRepository, WebDriver driver) {
        super(vacancyRepository, driver);
    }

    @Override
    public String getProviderName() {
        return PROVIDER;
    }

    @Override
    public List<Vacancy> scrapeAndSave(QueryParameters queryParameters) {
        Optional<Vacancy> last = getLastSaved(PROVIDER);
        String lastSavedUrl = last.map(Vacancy::getUrl).orElse(null);

        try (DouVacancyListPageLoader listLoader = new DouVacancyListPageLoader(webDriver);
             DouVacancyDetailsPageLoader detailsLoader = new DouVacancyDetailsPageLoader(webDriver)) {

            List<DouVacancyItem> loadedFromGlobalPage = listLoader.fetchVacancies(queryParameters);
            DouVacancyItem lastSaved = loadedFromGlobalPage.stream().filter(item -> lastSavedUrl != null && lastSavedUrl.equals(item.getVacancyUrl())).findFirst().orElse(null);
            int indexOfLaseSaved = loadedFromGlobalPage.contains(lastSaved) ? loadedFromGlobalPage.indexOf(lastSaved) : loadedFromGlobalPage.size();

            if (loadedFromGlobalPage.isEmpty()) {
                log.info("[DOU] No new vacancies found");
                return List.of();
            }

            List<Vacancy> populatedVacanciesFromDetailsPage = getVacanciesDetails(loadedFromGlobalPage.subList(0, indexOfLaseSaved), detailsLoader);

            List<Vacancy> saved = saveAll(populatedVacanciesFromDetailsPage);
            log.info("[DOU] Saved {} new vacancies", saved.size());

            return saved;
        } catch (Exception e) {
            log.warn("[DOU] Scraping error: {}", e.getMessage());
            return List.of();
        } finally {
            try {
                if (webDriver != null) webDriver.quit();
            } catch (Exception ignored) {
            }
        }
    }

    private List<Vacancy> getVacanciesDetails(List<DouVacancyItem> newItems, DouVacancyDetailsPageLoader detailsLoader) {
        List<Vacancy> toPersist = new ArrayList<>(newItems.size());
        for (DouVacancyItem it : newItems) {
            DouVacancyItem enriched = detailsLoader.enrichVacancyItem(it);
            toPersist.add(mapToVacancy(enriched));
        }
        return toPersist;
    }

    private Vacancy mapToVacancy(DouVacancyItem item) {

        return Vacancy.builder()
                .source(PROVIDER)
                .status(VacancyStatus.OPEN)
                .position(item.getVacancyTitle())
                .companyName(item.getCompanyName())
                .url(item.getVacancyUrl())
                .remoteAllowed(item.getVacancyLocation().contains("remote"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}

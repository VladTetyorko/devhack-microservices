package com.vladte.devhack.parser.service.provider.impl;

import com.vladte.devhack.entities.enums.VacancyStatus;
import com.vladte.devhack.entities.global.Vacancy;
import com.vladte.devhack.parser.entities.QueryParameters;
import com.vladte.devhack.parser.entities.items.DouVacancyItem;
import com.vladte.devhack.parser.repository.VacancyRepository;
import com.vladte.devhack.parser.service.provider.AbstractVacancyScraper;
import com.vladte.devhack.parser.service.selenium.dou.DouVacancyDetailsPageLoader;
import com.vladte.devhack.parser.service.selenium.dou.DouVacancyListPageLoader;
import com.vladte.devhack.parser.util.UaDateParsing;
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
        String lastSavedUrl = getLastSaved(PROVIDER).map(Vacancy::getUrl).orElse(null);

        try (DouVacancyListPageLoader listLoader = new DouVacancyListPageLoader(webDriver);
             DouVacancyDetailsPageLoader detailsLoader = new DouVacancyDetailsPageLoader(webDriver)) {

            List<DouVacancyItem> listPageItems = listLoader.fetchVacancies(queryParameters);
            if (listPageItems.isEmpty()) {
                log.info("[DOU] No vacancies found on the list page.");
                return List.of();
            }

            int indexOfLastSaved = findIndexOfUrl(listPageItems, lastSavedUrl);
            if (indexOfLastSaved == 0) {
                log.info("[DOU] No new vacancies since last saved one.");
                return List.of();
            } else if (indexOfLastSaved == -1) {
                log.info("[DOU] No saved vacancies found. Skipping scraping...");
                return List.of();
            }

            List<DouVacancyItem> newItems = listPageItems.subList(0, indexOfLastSaved);

            List<Vacancy> detailed = newItems.stream()
                    .map(detailsLoader::enrichVacancyItem)
                    .map(this::mapToVacancy)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

            List<Vacancy> saved = saveBatched(detailed);
            log.info("[DOU] Saved {} new vacancies.", saved.size());
            return saved;

        } catch (Exception e) {
            log.warn("[DOU] Scraping error: {}", e.getMessage(), e);
            return List.of();
        } finally {
            closeDriverQuietly();
        }
    }

    private int findIndexOfUrl(List<DouVacancyItem> items, String url) {
        if (url == null) return items.size();
        for (int i = 0; i < items.size(); i++) {
            if (url.equals(items.get(i).getVacancyUrl())) return i;
        }
        return items.size();
    }

    private Vacancy mapToVacancy(DouVacancyItem item) {
        LocalDateTime now = LocalDateTime.now();
        return Vacancy.builder()
                .source(PROVIDER)
                .status(VacancyStatus.OPEN)
                .position(item.getVacancyTitle())
                .companyName(item.getCompanyName())
                .description(item.getVacancyDescription())
                .url(item.getVacancyUrl())
                .remoteAllowed(isRemoteAllowed(item.getVacancyLocation()))
                .createdAt(UaDateParsing.parseUaToLdt(item.getVacancyDateText()))
                .updatedAt(now)
                .build();
    }

    private boolean isRemoteAllowed(String location) {
        return location != null && location.toLowerCase().contains("remote");
    }

    private void closeDriverQuietly() {
        try {
            Optional.ofNullable(webDriver).ifPresent(WebDriver::quit);
        } catch (Exception ignored) {
        }
    }
}

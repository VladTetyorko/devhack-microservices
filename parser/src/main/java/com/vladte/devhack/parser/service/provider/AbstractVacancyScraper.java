package com.vladte.devhack.parser.service.provider;

import com.vladte.devhack.domain.entities.global.Vacancy;
import com.vladte.devhack.domain.repository.global.VacancyRepository;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AbstractVacancyScraper implements ProviderScraper {

    private static final int BATCH_SIZE = 10;

    protected final VacancyRepository vacancyRepository;
    protected final WebDriver webDriver;

    protected Optional<Vacancy> getLastSaved(String provider) {
        return vacancyRepository.findLastSavedVacancyForProvider(provider);
    }

    protected List<Vacancy> saveAll(List<Vacancy> items) {
        NavigableMap<String, Vacancy> alreadyExisting = getSavedVacancyMap();
        List<Vacancy> distinct = items.stream().filter(item ->
                !alreadyExisting.containsKey(item.getUrl())
        ).toList();
        return vacancyRepository.saveAll(distinct);
    }

    protected List<Vacancy> saveBatched(List<Vacancy> items) {
        if (items == null || items.isEmpty()) return List.of();

        NavigableMap<String, Vacancy> alreadyExisting = getSavedVacancyMap();
        Set<String> seenUrls = new HashSet<>();
        List<Vacancy> distinct = new ArrayList<>(items.size());
        for (Vacancy v : items) {
            String url = v.getUrl();
            if (url == null || url.isBlank()) continue;
            if (alreadyExisting.containsKey(url)) continue;
            if (seenUrls.add(url)) {
                distinct.add(v);
            }
        }

        if (distinct.isEmpty()) return List.of();

        List<Vacancy> savedAll = new ArrayList<>(distinct.size());
        for (int i = 0; i < distinct.size(); i += BATCH_SIZE) {
            int toIndex = Math.min(i + BATCH_SIZE, distinct.size()); // exclusive
            List<Vacancy> batch = distinct.subList(i, toIndex);
            savedAll.addAll(vacancyRepository.saveAll(batch));
        }
        return savedAll;
    }

    protected NavigableMap<String, Vacancy> getSavedVacancyMap() {
        Comparator<String> urlComparator = Comparator.nullsLast(String::compareTo);

        return vacancyRepository.findAll()
                .stream()
                .filter(v -> v.getUrl() != null && !v.getUrl().isBlank())
                .collect(Collectors.toMap(
                        Vacancy::getUrl,
                        Function.identity(),
                        (a, b) -> a,
                        () -> new TreeMap<>(urlComparator)
                ));
    }


    protected Vacancy buildVacancy(String provider, String companyName, String title, String url, LocalDateTime openAt) {
        Vacancy v = new Vacancy();
        v.setCompanyName(companyName);
        v.setPosition(title);
        v.setSource(provider);
        v.setUrl(url);
        v.setOpenAt(openAt);
        return v;
    }
}

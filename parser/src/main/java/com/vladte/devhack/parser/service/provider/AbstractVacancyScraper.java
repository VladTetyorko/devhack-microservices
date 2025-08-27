package com.vladte.devhack.parser.service.provider;

import com.vladte.devhack.entities.global.Vacancy;
import com.vladte.devhack.parser.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractVacancyScraper implements ProviderScraper {

    protected final VacancyRepository vacancyRepository;
    protected final WebDriver webDriver;

    protected Optional<Vacancy> getLastSaved(String provider) {
        return vacancyRepository.findLastSavedVacancyForProvider(provider);
    }

    protected List<Vacancy> saveAll(List<Vacancy> items) {
        return vacancyRepository.saveAll(items);
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

package com.vladte.devhack.parser.service;

import com.vladte.devhack.entities.global.Vacancy;
import com.vladte.devhack.parser.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for handling the parsing and persistence of vacancies.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VacancyService {

    private final VacancyRepository vacancyRepository;
    private final VacancyParserFactory parserFactory;

    /**
     * Parse HTML content and save the extracted vacancies.
     *
     * @param source      The source of the HTML content
     * @param htmlContent The HTML content to parse
     * @return The number of vacancies saved
     */
    @Transactional
    public int parseAndSave(String source, String htmlContent) {
        log.info("Parsing and saving vacancies from source: {}", source);

        Optional<VacancyParser> parserOpt = parserFactory.getParser(source);
        if (parserOpt.isEmpty()) {
            log.error("No parser found for source: {}", source);
            return 0;
        }

        VacancyParser parser = parserOpt.get();
        List<Vacancy> vacancies = parser.parse(htmlContent);

        if (vacancies.isEmpty()) {
            log.warn("No vacancies found in HTML content from source: {}", source);
            return 0;
        }

        log.info("Found {} vacancies from source: {}", vacancies.size(), source);

        // Filter out vacancies that already exist (by URL)
        List<Vacancy> newVacancies = vacancies.stream()
                .filter(vacancy -> {
                    if (vacancy.getUrl() == null) {
                        return true; // Keep vacancies without URL
                    }
                    return vacancyRepository.findByUrl(vacancy.getUrl()).isEmpty();
                })
                .toList();

        if (newVacancies.isEmpty()) {
            log.info("All vacancies already exist in the database");
            return 0;
        }

        log.info("Saving {} new vacancies", newVacancies.size());
        vacancyRepository.saveAll(newVacancies);

        return newVacancies.size();
    }

    /**
     * Get all vacancies.
     *
     * @return A list of all vacancies
     */
    @Transactional(readOnly = true)
    public List<Vacancy> getAllVacancies() {
        return vacancyRepository.findAll();
    }

    /**
     * Get vacancies by source.
     *
     * @param source The source to filter by
     * @return A list of vacancies from the specified source
     */
    @Transactional(readOnly = true)
    public List<Vacancy> getVacanciesBySource(String source) {
        return vacancyRepository.findBySource(source);
    }
}
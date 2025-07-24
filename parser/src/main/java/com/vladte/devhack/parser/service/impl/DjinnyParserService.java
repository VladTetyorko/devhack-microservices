package com.vladte.devhack.parser.service.impl;

import com.vladte.devhack.entities.global.Vacancy;
import com.vladte.devhack.parser.service.VacancyParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for parsing job vacancies from Djinny website.
 */
@Service
@Slf4j
public class DjinnyParserService implements VacancyParser {

    private static final String SOURCE = "Djinny";

    /**
     * Parse HTML content from Djinny website to extract job vacancies.
     *
     * @param htmlContent The HTML content to parse
     * @return A list of extracted vacancies
     */
    @Override
    public List<Vacancy> parse(String htmlContent) {
        log.info("Parsing HTML content from Djinny");
        List<Vacancy> vacancies = new ArrayList<>();

        try {
            Document document = Jsoup.parse(htmlContent);
            Elements jobElements = document.select(".job-list-item");

            for (Element jobElement : jobElements) {
                try {
                    Vacancy vacancy = new Vacancy();

                    // Set source
                    vacancy.setSource(SOURCE);

                    // Extract company name
                    Element companyElement = jobElement.selectFirst(".company-name");
                    if (companyElement != null) {
                        vacancy.setCompanyName(companyElement.text().trim());
                    }

                    // Extract position
                    Element positionElement = jobElement.selectFirst(".job-title");
                    if (positionElement != null) {
                        vacancy.setPosition(positionElement.text().trim());
                    }

                    // Extract technologies
                    Element techElement = jobElement.selectFirst(".job-tags");
                    if (techElement != null) {
                        vacancy.setTechnologies(techElement.text().trim());
                    }

                    // Extract URL
                    Element linkElement = jobElement.selectFirst("a.job-link");
                    if (linkElement != null) {
                        String relativeUrl = linkElement.attr("href");
                        vacancy.setUrl("https://djinny.co" + relativeUrl);
                    }

                    // Extract remote status if available
                    Element remoteElement = jobElement.selectFirst(".remote-tag");
                    vacancy.setRemoteAllowed(remoteElement != null);

                    // Set created timestamp
                    vacancy.setCreatedAt(LocalDateTime.now());
                    vacancy.setUpdatedAt(LocalDateTime.now());

                    vacancies.add(vacancy);
                } catch (Exception e) {
                    log.error("Error parsing job element: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error parsing Djinny HTML: {}", e.getMessage());
        }

        log.info("Extracted {} vacancies from Djinny", vacancies.size());
        return vacancies;
    }
}
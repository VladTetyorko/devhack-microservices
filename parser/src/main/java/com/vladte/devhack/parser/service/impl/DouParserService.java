package com.vladte.devhack.parser.service.impl;

import com.vladte.devhack.entities.Vacancy;
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
 * Service for parsing job vacancies from DOU website.
 */
@Service
@Slf4j
public class DouParserService implements VacancyParser {

    private static final String SOURCE = "DOU";

    /**
     * Parse HTML content from DOU website to extract job vacancies.
     *
     * @param htmlContent The HTML content to parse
     * @return A list of extracted vacancies
     */
    @Override
    public List<Vacancy> parse(String htmlContent) {
        log.info("Parsing HTML content from DOU");
        List<Vacancy> vacancies = new ArrayList<>();

        try {
            Document document = Jsoup.parse(htmlContent);
            Elements jobElements = document.select(".vacancy");

            for (Element jobElement : jobElements) {
                try {
                    Vacancy vacancy = new Vacancy();

                    // Set source
                    vacancy.setSource(SOURCE);

                    // Extract company name
                    Element companyElement = jobElement.selectFirst(".company");
                    if (companyElement != null) {
                        vacancy.setCompanyName(companyElement.text().trim());
                    }

                    // Extract position
                    Element positionElement = jobElement.selectFirst(".title");
                    if (positionElement != null) {
                        vacancy.setPosition(positionElement.text().trim());
                    }

                    // Extract technologies
                    Element techElement = jobElement.selectFirst(".tags");
                    if (techElement != null) {
                        vacancy.setTechnologies(techElement.text().trim());
                    }

                    // Extract URL
                    Element linkElement = jobElement.selectFirst("a.vt");
                    if (linkElement != null) {
                        vacancy.setUrl(linkElement.attr("href"));
                    }

                    // Extract remote status if available
                    Element remoteElement = jobElement.selectFirst(".remote");
                    vacancy.setRemoteAllowed(remoteElement != null);

                    // Extract contact person if available
                    Element contactElement = jobElement.selectFirst(".recruiter");
                    if (contactElement != null) {
                        vacancy.setContactPerson(contactElement.text().trim());
                    }

                    // Set created timestamp
                    vacancy.setCreatedAt(LocalDateTime.now());
                    vacancy.setUpdatedAt(LocalDateTime.now());

                    vacancies.add(vacancy);
                } catch (Exception e) {
                    log.error("Error parsing job element: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error parsing DOU HTML: {}", e.getMessage());
        }

        log.info("Extracted {} vacancies from DOU", vacancies.size());
        return vacancies;
    }
}
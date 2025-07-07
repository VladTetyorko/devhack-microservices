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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for parsing job vacancies from LinkedIn website.
 */
@Service
@Slf4j
public class LinkedinParserService implements VacancyParser {

    private static final String SOURCE = "LinkedIn";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");

    /**
     * Parse HTML content from LinkedIn website to extract job vacancies.
     *
     * @param htmlContent The HTML content to parse
     * @return A list of extracted vacancies
     */
    @Override
    public List<Vacancy> parse(String htmlContent) {
        log.info("Parsing HTML content from LinkedIn");
        List<Vacancy> vacancies = new ArrayList<>();

        try {
            Document document = Jsoup.parse(htmlContent);
            Elements jobElements = document.select(".job-card-container");

            for (Element jobElement : jobElements) {
                try {
                    Vacancy vacancy = new Vacancy();

                    // Set source
                    vacancy.setSource(SOURCE);

                    // Extract company name
                    Element companyElement = jobElement.selectFirst(".job-card-container__company-name");
                    if (companyElement != null) {
                        vacancy.setCompanyName(companyElement.text().trim());
                    }

                    // Extract position
                    Element positionElement = jobElement.selectFirst(".job-card-list__title");
                    if (positionElement != null) {
                        vacancy.setPosition(positionElement.text().trim());
                    }

                    // Extract URL
                    Element linkElement = jobElement.selectFirst("a.job-card-container__link");
                    if (linkElement != null) {
                        String relativeUrl = linkElement.attr("href");
                        vacancy.setUrl("https://www.linkedin.com" + relativeUrl);
                    }

                    // Extract job description to find technologies and other details
                    Element descriptionElement = jobElement.selectFirst(".job-card-container__description");
                    if (descriptionElement != null) {
                        String description = descriptionElement.text().trim();

                        // Try to extract technologies from description
                        // This is a simplified approach - in a real implementation, 
                        // you might use NLP or a predefined list of technologies
                        if (description.contains("technologies:") || description.contains("tech stack:")) {
                            int techIndex = Math.max(
                                    description.indexOf("technologies:"),
                                    description.indexOf("tech stack:")
                            );
                            if (techIndex > 0) {
                                int endIndex = description.indexOf(".", techIndex);
                                if (endIndex > techIndex) {
                                    String techText = description.substring(techIndex, endIndex).trim();
                                    vacancy.setTechnologies(techText);
                                }
                            }
                        }

                        // Try to extract email from description
                        Matcher matcher = EMAIL_PATTERN.matcher(description);
                        if (matcher.find()) {
                            vacancy.setContactEmail(matcher.group());
                        }

                        // Check for remote work mentions
                        vacancy.setRemoteAllowed(
                                description.toLowerCase().contains("remote") ||
                                        description.toLowerCase().contains("work from home")
                        );
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
            log.error("Error parsing LinkedIn HTML: {}", e.getMessage());
        }

        log.info("Extracted {} vacancies from LinkedIn", vacancies.size());
        return vacancies;
    }
}
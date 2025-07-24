package com.vladte.devhack.parser.service;

import com.vladte.devhack.entities.global.Vacancy;

import java.util.List;

/**
 * Interface for parsing job vacancies from HTML content.
 * Implementations should handle specific job websites.
 */
public interface VacancyParser {
    /**
     * Parse HTML content to extract job vacancies.
     *
     * @param htmlContent The HTML content to parse
     * @return A list of extracted vacancies
     */
    List<Vacancy> parse(String htmlContent);
}
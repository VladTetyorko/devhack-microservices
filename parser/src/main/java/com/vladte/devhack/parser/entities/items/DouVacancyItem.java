package com.vladte.devhack.parser.entities.items;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single vacancy item listed on DOU vacancies list page
 * and enriched by details page loader. Extracted via Selenium using provided selectors.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouVacancyItem {
    /**
     * Raw date label text displayed at the left of the vacancy item (list page)
     */
    private String dateText;

    /**
     * Vacancy title text (list or details page)
     */
    private String vacancyTitle;

    /**
     * Vacancy details page URL
     */
    private String vacancyUrl;

    /**
     * Company display name
     */
    private String companyName;

    /**
     * Company link URL (if present)
     */
    private String companyUrl;

    /**
     * City or list of cities text (list page)
     */
    private String citiesText;

    /**
     * Salary text (if present)
     */
    private String salaryText;

    /**
     * Short description/snippet (list page)
     */
    private String snippet;

    // ===== Details page enrichment fields =====

    /**
     * Link to company logo (href on logo anchor)
     */
    private String companyLogoLink;

    /**
     * Link to company's all jobs page
     */
    private String companyAllJobsUrl;

    /**
     * Company description text from details page
     */
    private String companyDescription;

    /**
     * Breadcrumbs text from details page
     */
    private String breadcrumbs;

    /**
     * Vacancy date text from details page
     */
    private String vacancyDateText;

    /**
     * Vacancy location text from details page
     */
    private String vacancyLocation;

    /**
     * Vacancy description text from details page
     */
    private String vacancyDescription;

    /**
     * External apply button link (if present)
     */
    private String applyUrl;
}

package com.vladte.devhack.parser.service.selenium.dou;

import com.vladte.devhack.parser.entities.items.DouVacancyItem;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Page loader for DOU vacancy details page using Selenium.
 * Loads a single vacancy details page and enriches DouVacancyItem with details info.
 */
@Component
public class DouVacancyDetailsPageLoader implements AutoCloseable {

    // Main vacancy container
    private static final By VACANCY_CONTAINER = By.cssSelector(".b-vacancy");

    // Company info
    private static final By COMPANY_LOGO_LINK = By.cssSelector(".b-compinfo a.logo");
    private static final By COMPANY_NAME = By.cssSelector(".b-compinfo .info .l-n a:first-child");
    private static final By COMPANY_ALL_JOBS = By.cssSelector(".b-compinfo .info .l-n a.all-v");
    private static final By COMPANY_DESCRIPTION = By.cssSelector(".b-compinfo .info .l-t");

    // Breadcrumbs
    private static final By BREADCRUMBS = By.cssSelector("ul.b-hr-admin-panel li.breadcrumbs");

    // Vacancy details
    private static final By VACANCY_DATE = By.cssSelector(".l-vacancy .date");
    private static final By VACANCY_TITLE = By.cssSelector(".l-vacancy h1.g-h2");
    private static final By VACANCY_LOCATION = By.cssSelector(".l-vacancy .sh-info .place");
    private static final By VACANCY_DESCRIPTION = By.cssSelector(".l-vacancy .b-typo.vacancy-section");

    // Apply link
    private static final By APPLY_BUTTON = By.cssSelector(".reply a.replied-external");

    private final WebDriver webDriver;
    private final WebDriverWait wait;

    public DouVacancyDetailsPageLoader(WebDriver webDriver) {
        this.webDriver = webDriver;
        this.wait = new WebDriverWait(this.webDriver, Duration.ofSeconds(15));
    }

    /**
     * Loads details page for the given vacancy item and fills details fields.
     *
     * @param item DouVacancyItem with vacancyUrl set.
     * @return the same item instance enriched with details (or unchanged if URL is missing or load fails)
     */
    public DouVacancyItem enrichVacancyItem(DouVacancyItem item) {
        if (item == null || item.getVacancyUrl() == null || item.getVacancyUrl().isBlank()) {
            return item;
        }
        try {
            webDriver.get(item.getVacancyUrl());
            wait.until(ExpectedConditions.presenceOfElementLocated(VACANCY_CONTAINER));

            // Company info
            WebElement logoAnchor = findOptional(COMPANY_LOGO_LINK);
            if (logoAnchor != null) {
                String href = safeGetAttr(logoAnchor, "href");
                if (!href.isBlank()) item.setCompanyLogoLink(href);
            }
            WebElement companyNameEl = findOptional(COMPANY_NAME);
            if (companyNameEl != null) {
                String name = companyNameEl.getText().trim();
                if (!name.isBlank()) item.setCompanyName(name);
                // Also company URL if the anchor has href
                String href = safeGetAttr(companyNameEl, "href");
                if (!href.isBlank()) item.setCompanyUrl(href);
            }
            WebElement allJobsAnchor = findOptional(COMPANY_ALL_JOBS);
            if (allJobsAnchor != null) {
                String href = safeGetAttr(allJobsAnchor, "href");
                if (!href.isBlank()) item.setCompanyAllJobsUrl(href);
            }
            WebElement companyDescEl = findOptional(COMPANY_DESCRIPTION);
            if (companyDescEl != null) {
                item.setCompanyDescription(companyDescEl.getText().trim());
            }

            // Breadcrumbs
            WebElement breadcrumbsEl = findOptional(BREADCRUMBS);
            if (breadcrumbsEl != null) {
                item.setBreadcrumbs(breadcrumbsEl.getText().trim());
            }

            // Vacancy details
            WebElement dateEl = findOptional(VACANCY_DATE);
            if (dateEl != null) item.setVacancyDateText(dateEl.getText().trim());

            WebElement titleEl = findOptional(VACANCY_TITLE);
            if (titleEl != null) item.setVacancyTitle(titleEl.getText().trim());

            WebElement placeEl = findOptional(VACANCY_LOCATION);
            if (placeEl != null) item.setVacancyLocation(placeEl.getText().trim());

            WebElement descrEl = findOptional(VACANCY_DESCRIPTION);
            if (descrEl != null) {
                String text = descrEl.getText().trim();
                item.setVacancyDescription(text);
            }

            // Apply link
            WebElement applyEl = findOptional(APPLY_BUTTON);
            if (applyEl != null) {
                String href = safeGetAttr(applyEl, "href");
                if (!href.isBlank()) item.setApplyUrl(href);
            }
        } catch (TimeoutException e) {
            // ignore - return item as is
        } catch (Exception e) {
            // ignore but keep item unchanged
        }
        return item;
    }

    private WebElement findOptional(By selector) {
        try {
            return webDriver.findElement(selector);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private String safeGetAttr(WebElement el, String name) {
        try {
            String v = el.getAttribute(name);
            return v != null ? v.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void close() {
        if (webDriver != null) {
            try {
                webDriver.quit();
            } catch (Exception ignored) {
            }
        }
    }
}

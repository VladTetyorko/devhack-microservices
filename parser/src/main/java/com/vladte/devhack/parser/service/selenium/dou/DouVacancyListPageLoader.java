package com.vladte.devhack.parser.service.selenium.dou;

import com.vladte.devhack.parser.entities.QueryParameters;
import com.vladte.devhack.parser.entities.items.DouVacancyItem;
import com.vladte.devhack.parser.service.selenium.AbstractSeleniumSupport;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Page loader for DOU vacancies list page using Selenium.
 * Opens the vacancies list, handles pagination ("More jobs" button),
 * and extracts structured vacancy items.
 */
@Component
public class DouVacancyListPageLoader extends AbstractSeleniumSupport {

    private static final Logger log = LoggerFactory.getLogger(DouVacancyListPageLoader.class);

    public static final String DEFAULT_JAVA_VACANCY_URL = "https://jobs.dou.ua/vacancies/?category=Java";

    // CSS selectors for vacancy elements
    private static final By VACANCY_LIST_ITEMS = By.cssSelector("#vacancyListId ul.lt > li.l-vacancy");
    private static final By DATE_LABEL = By.cssSelector(".date");
    private static final By VACANCY_TITLE_LINK = By.cssSelector(".title a.vt");
    private static final By COMPANY_LINK = By.cssSelector(".title strong a.company");
    private static final By CITIES_LABEL = By.cssSelector(".title .cities");
    private static final By SALARY_LABEL = By.cssSelector(".title .salary");
    private static final By SNIPPET_TEXT = By.cssSelector(".sh-info");
    private static final By MORE_JOBS_BUTTON = By.cssSelector(".more-btn > a");

    public DouVacancyListPageLoader(WebDriver webDriver) {
        super(webDriver, Duration.ofSeconds(15));
    }

    /**
     * Loads the vacancy list page and optionally paginates with "More jobs" clicks.
     *
     * @param queryParameters query parameters to filter the vacancies list
     * @return list of parsed vacancies
     */
    public List<DouVacancyItem> fetchVacancies(QueryParameters queryParameters) {
        String startUrl = Optional.ofNullable(queryParameters.getUrl()).orElse(DEFAULT_JAVA_VACANCY_URL);
        log.info("Starting to fetch vacancies from URL: {}", startUrl);

        String targetUrl = populateUrlWithFilers(startUrl, queryParameters.getFilters());
        log.debug("Target URL with filters: {}", targetUrl);

        webDriver.get(targetUrl);
        log.debug("Page loaded successfully");

        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(VACANCY_LIST_ITEMS));

        for (int i = 0; i < queryParameters.getMaxClicks(); i++) {
            int currentCount = webDriver.findElements(VACANCY_LIST_ITEMS).size();
            log.debug("Current vacancy count: {}", currentCount);

            if (!clickMoreJobsButton()) {
                log.debug("No more jobs button found or not clickable");
                break;
            }

            boolean newItemsLoaded = waitUntilListExpands(VACANCY_LIST_ITEMS, currentCount, Duration.ofSeconds(10));
            if (!newItemsLoaded) {
                log.debug("No new items loaded after clicking 'More jobs'");
                break;
            }
            log.debug("Successfully loaded more vacancies");
        }

        List<WebElement> vacancyElements = webDriver.findElements(VACANCY_LIST_ITEMS);
        List<DouVacancyItem> vacancies = new ArrayList<>(vacancyElements.size());
        log.info("Found {} vacancy elements", vacancyElements.size());

        for (WebElement vacancyElement : vacancyElements) {
            vacancies.add(parseVacancyItem(vacancyElement));
        }
        log.info("Successfully parsed {} vacancies", vacancies.size());
        return vacancies;
    }

    private String populateUrlWithFilers(String targetUrl, Map<String, String> filters) {
        StringBuilder urlBuilder = new StringBuilder(targetUrl);
        if (filters != null && !filters.isEmpty()) {
            urlBuilder.append("?");
            filters.forEach((key, value) -> urlBuilder.append(key).append("=").append(value).append("&"));
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        return urlBuilder.toString();
    }

    private boolean clickMoreJobsButton() {
        try {
            WebElement button = webDriver.findElement(MORE_JOBS_BUTTON);
            scrollToElement(button);
            wait.until(ExpectedConditions.elementToBeClickable(button));
            button.click();
            return true;
        } catch (NoSuchElementException | ElementClickInterceptedException | TimeoutException e) {
            log.debug("Failed to click 'More jobs' button: {}", e.getMessage());
            return false;
        }
    }

    private DouVacancyItem parseVacancyItem(WebElement listItem) {
        String dateText = extractText(listItem, DATE_LABEL);

        WebElement titleAnchor = findChild(listItem, VACANCY_TITLE_LINK);
        String title = titleAnchor != null ? titleAnchor.getText().trim() : "";
        String vacancyUrl = titleAnchor != null ? titleAnchor.getAttribute("href") : "";

        WebElement companyAnchor = findChild(listItem, COMPANY_LINK);
        String companyName = companyAnchor != null ? companyAnchor.getText().trim() : "";
        String companyUrl = companyAnchor != null ? companyAnchor.getAttribute("href") : "";

        String cities = extractText(listItem, CITIES_LABEL);
        String salary = extractText(listItem, SALARY_LABEL);
        String snippet = extractText(listItem, SNIPPET_TEXT);

        log.debug("Parsed vacancy: {} at {}", title, companyName);

        return DouVacancyItem.builder()
                .dateText(dateText)
                .vacancyTitle(title)
                .vacancyUrl(vacancyUrl)
                .companyName(companyName)
                .companyUrl(companyUrl)
                .citiesText(cities)
                .salaryText(salary)
                .snippet(snippet)
                .build();
    }

    @Override
    public void close() {
        if (webDriver != null) {
            try {
                webDriver.quit();
                log.debug("WebDriver closed successfully");
            } catch (Exception e) {
                log.warn("Error closing WebDriver: {}", e.getMessage());
            }
        }
    }
}
package com.vladte.devhack.parser.service.selenium.djinni;

import com.vladte.devhack.parser.entities.QueryParameters;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock Djinni list page loader. Replace with real Selenium later.
 */
@Component
public class DjinniVacancyListPageLoader {

    public record Item(String title, String company, String url) {
    }

    public List<Item> fetchVacancies(QueryParameters params) {
        // mock: return empty or a couple of sample items
        return new ArrayList<>();
    }
}

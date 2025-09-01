package com.vladte.devhack.parser.service.selenium.linkedin;

import com.vladte.devhack.parser.entities.QueryParameters;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock LinkedIn list page loader. Replace with real Selenium later.
 */
@Component
public class LinkedInVacancyListPageLoader {

    public record Item(String title, String company, String url) {
    }

    public List<Item> fetchVacancies(QueryParameters params) {
        // mock: return empty or a couple of sample items
        return new ArrayList<>();
    }
}

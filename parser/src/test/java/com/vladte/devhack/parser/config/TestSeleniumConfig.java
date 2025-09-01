package com.vladte.devhack.parser.config;

import com.vladte.devhack.parser.service.selenium.dou.DouVacancyDetailsPageLoader;
import com.vladte.devhack.parser.service.selenium.dou.DouVacancyListPageLoader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Test configuration providing Selenium WebDriver and PageLoader beans
 * for integration tests that use real data.
 */
@Configuration
public class TestSeleniumConfig {

    @Bean(destroyMethod = "quit")
    @Lazy
    public WebDriver testWebDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless=new",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1920,1080"
        );
        return new ChromeDriver(options);
    }

    @Bean
    @Lazy
    public DouVacancyListPageLoader douVacancyListPageLoader(WebDriver testWebDriver) {
        return new DouVacancyListPageLoader(testWebDriver);
    }

    @Bean
    @Lazy
    public DouVacancyDetailsPageLoader douVacancyDetailsPageLoader(WebDriver testWebDriver) {
        return new DouVacancyDetailsPageLoader(testWebDriver);
    }
}

package com.vladte.devhack.parser.service.selenium;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Common Selenium helper methods to be shared across page loaders.
 */
public abstract class AbstractSeleniumSupport implements AutoCloseable {

    protected final WebDriver webDriver;
    protected final WebDriverWait wait;

    protected AbstractSeleniumSupport(WebDriver webDriver, Duration waitTimeout) {
        this.webDriver = webDriver;
        this.wait = new WebDriverWait(this.webDriver, waitTimeout);
    }

    protected void scrollToElement(WebElement element) {
        try {
            ((JavascriptExecutor) webDriver)
                    .executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        } catch (Exception ignored) {
        }
    }

    protected boolean waitUntilListExpands(By selector, int previousCount, Duration timeout) {
        try {
            WebDriverWait shortWait = new WebDriverWait(webDriver, timeout);
            return shortWait.until((ExpectedCondition<Boolean>) driver -> {
                int newCount = driver.findElements(selector).size();
                return newCount > previousCount;
            });
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected WebElement findChild(WebElement parent, By selector) {
        try {
            return parent.findElement(selector);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    protected String extractText(WebElement parent, By selector) {
        WebElement element = findChild(parent, selector);
        return element != null ? element.getText().trim() : "";
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

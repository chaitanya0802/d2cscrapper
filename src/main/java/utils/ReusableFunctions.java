package utils;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.Set;

public class ReusableFunctions {

    private WebDriver driver;
    private static final int DEFAULT_WAIT = 10; // seconds
    private static final int CLICK_WAIT = 20;   // seconds

    public ReusableFunctions(WebDriver driver) {
        this.driver = driver;
    }

    // ================= Browser Operations =================
    public void openApplicationUrl() {
        String url = ConfigReader.getProperty("baseurl");
        if (url == null || url.isEmpty()) {
            throw new RuntimeException("Base URL is missing in config.");
        }
        driver.get(url);
    }

    public void closeBrowser() {
        if (driver != null) driver.quit();
    }

    public void setImplicitWait(long seconds) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(seconds));
    }

    // ================= Wait Helpers =================
    private WebDriverWait getWait(int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    public void waitForVisibility(WebElement element) {
        getWait(DEFAULT_WAIT).until(ExpectedConditions.visibilityOf(element));
    }

    public void waitForClickability(WebElement element) {
        getWait(CLICK_WAIT).until(ExpectedConditions.elementToBeClickable(element));
    }

    public void waitForPresence(By locator) {
        getWait(DEFAULT_WAIT).until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    // ================= Element Operations =================
    public void clickElement(WebElement element) {
        waitForClickability(element);
        element.click();
    }

    public void enterText(WebElement element, String text) {
        waitForVisibility(element);
        element.clear();
        element.sendKeys(text);
    }

    public String getText(WebElement element) {
        waitForVisibility(element);
        String text = element.getText().trim();
        if (text.isEmpty()) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            text = (String) js.executeScript(
                "return arguments[0].innerText || arguments[0].textContent;", element
            );
            text = text != null ? text.trim() : "";
        }
        return text;
    }

    public void selectOption(WebElement element, String option) {
        waitForVisibility(element);
        Select sel = new Select(element);
        sel.selectByVisibleText(option);
    }

    // ================= Scroll Helpers =================
    public void scrollAtBottom(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(false);", element);
    }

    public void scrollIntoViewAndClick(WebElement element) {
        scrollAtBottom(element);
        waitForClickability(element);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", element);
    }

    public void scrollByOffset(int x, int y) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(arguments[0], arguments[1]);", x, y);
    }

    // ================= Window Handling =================
    public void switchToNewWindowAndBack(WebElement triggerElement) {
        String mainWindow = driver.getWindowHandle();
        triggerElement.click();
        Set<String> allWindows = driver.getWindowHandles();
        for (String win : allWindows) {
            if (!win.equals(mainWindow)) driver.switchTo().window(win);
        }
        driver.switchTo().window(mainWindow);
    }

    // ================= Alert Handling =================
    public void handleAlert(String inputValue) {
        Alert alert = driver.switchTo().alert();
        if (inputValue != null && !inputValue.isEmpty()) alert.sendKeys(inputValue);
        alert.accept();
    }

    // ================= Misc =================
    public void printPageTitle() {
        System.out.println(driver.getTitle());
    }

    // ================= Utility =================
    public By getByFromElement(WebElement element) {
        String desc = element.toString();
        try {
            String locator = desc.substring(desc.indexOf("->") + 3, desc.length() - 1).trim();
            String[] parts = locator.split(": ");
            String strategy = parts[0].trim();
            String value = parts[1].trim();

            switch (strategy) {
                case "id": return By.id(value);
                case "name": return By.name(value);
                case "class name": return By.className(value);
                case "tag name": return By.tagName(value);
                case "link text": return By.linkText(value);
                case "partial link text": return By.partialLinkText(value);
                case "css selector": return By.cssSelector(value);
                case "xpath": return By.xpath(value);
                default: throw new IllegalArgumentException("Unknown locator strategy: " + strategy);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to derive By locator from WebElement", e);
        }
    }

}

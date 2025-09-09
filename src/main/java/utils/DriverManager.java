package utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;

public class DriverManager {

    private static WebDriver driver;

    private DriverManager() {
        // private constructor to prevent instantiation
    }

    /**
     * Initialize Chrome WebDriver using WebDriverManager
     */
    public static WebDriver initDriver() {
        if (driver == null) {
            // Setup ChromeDriver automatically
            WebDriverManager.chromedriver().setup();

            // Chrome options
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            options.addArguments("--disable-notifications");
            // options.addArguments("--headless=new"); // Uncomment if headless is needed

            // Initialize driver
            driver = new ChromeDriver(options);

            // Global implicit wait
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        }
        return driver;
    }

    /**
     * Get current driver instance
     */
    public static WebDriver getDriver() {
        if (driver == null) {
            return initDriver();
        }
        return driver;
    }

    /**
     * Quit driver safely
     */
    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}

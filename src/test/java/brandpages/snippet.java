
package brandpages;

import java.util.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.Test;
import io.github.bonigarcia.wdm.WebDriverManager;
import utils.*;

public class snippet {

    String maincat = "";
    //for offer
    List<String> subcat = List.of(
    );

    String storename = "";
    String baseUrl = "";

    static WebDriver driver;

    public snippet() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }

    @Test
    public void scrapController() {
        if (Boolean.parseBoolean(ConfigReader.getProperty("product"))) scrapProdData();

        if (Boolean.parseBoolean(ConfigReader.getProperty("offer"))) scrapOfferData();

    }

    public void scrapProdData() {
        try {
            


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //========================== offer

    public void scrapOfferData() {

        try {
            

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

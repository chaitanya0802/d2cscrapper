
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

public class mamaearth {

    String maincat = "Beauty and Personal Care";
    //for offer
    Map<String, String> subcat = Map.of(
        "FaceCare+FaceWash", "https://mamaearth.in/product-category/face-wash",
        "FaceCare+Sunscreen", "https://mamaearth.in/product-category/sunscreen",
        "FaceCare+Face Serum", "https://mamaearth.in/product-category/face-serum",
        "FaceCare+Face Cream", "https://mamaearth.in/product-category/face-cream",
        "FaceCare+Face Moisturizer", "https://mamaearth.in/product-category/face-moisturizer",
        "FaceCare+Face Mask", "https://mamaearth.in/product-category/face-mask",
        "FaceCare+Facial Kits", "https://mamaearth.in/product-category/facial-kits",
        "FaceCare+Face Scrub", "https://mamaearth.in/product-category/face-scrub",

        "HairCare+Shampoo", "https://mamaearth.in/product-category/hair-shampoo"
    );

    String storename = "Mamaearth";
    String baseUrl = "https://www.mamaearth.in";

    static WebDriver driver;

    public mamaearth() {
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

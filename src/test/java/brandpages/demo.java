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
import utils.ConfigReader;
import utils.Product;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import org.openqa.selenium.support.ui.WebDriverWait;


public class demo {

    String storename = "Mamaearth";
    String maincat = "BPC";

    // format = catname , catlink
    Map<String, String> subcat = Map.of(
             "Speakers+Party Speakers" , "https://www.boat-lifestyle.com/collections/party-speakers"
//            "FaceCare+FaceWash", "https://mamaearth.in/product-category/face-wash"
    );

    Map<String, String> boatlocators =  Map.of(
        "productsection", "//*[@id='facet-main']/product-list",
        "productitem", "//*[@id='Huratips_Loop']//product-item",

        "name", ".//div/div[2]/div/div/a",
        "price",".//div/div[2]/div/div/div[1]/div[1]/span[1]",
        "discount_percent" , ".//div/div[2]/div/div/div[1]/div[1]/p",
        "description", ".//div/div[2]/div/div/div[1]/div[2]",
        "url", ".//div/div[2]/div/div/a",
        "imageurl", ".//div/div[1]/div/a/img",
        "rating", ".//div/div[2]/div/span/div/div",
        "availablity_idf", "Add to cart"
    );

     Map<String, String> melocators = Map.of(
         "productsection", "//*[@id='__next']/div[6]/div[8]/section",
         "productitem", "//*[@id='__next']/div[6]/div[8]/section/section/div",

         "name", ".//div[2]/div/div[1]/div[1]",
         "price",".//div[2]/div/div[1]/div[5]/div[1]",
             "discount_percent" , ".//div[2]/div/div[1]/div[5]/div[3]",
         "description", ".//div[2]/div/div[1]/div[2]",
         "url", "routertype",
         "imageurl", ".//div[1]/div/img",
         "rating", ".//div[2]/div/div[1]/div[4]/span[1]",
         "availablity_idf", "Add To Cart"
     );

    WebDriver driver;

    @Test
    public void scrapController() {
        if (Boolean.parseBoolean(ConfigReader.getProperty("product")))
            scrapProdData();

        // if (Boolean.parseBoolean(ConfigReader.getProperty("offer")))
        // scrapOfferData();

    }

    //parse the file and scrap data
    public void scrapProdData() {

        // driver config
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();

        for (Map.Entry<String, String> m : subcat.entrySet()) {
            System.out.println("==> " + m.getKey() + " ==>" + m.getValue());
            scrapCategoryData(m.getKey(), m.getValue(), boatlocators);
        }
    }

    public void scrapCategoryData(String subcat, String catURL, Map<String, String> locators) {
        List<Product> productList = new ArrayList<>();
        int prevProductCount = 0, sameCountTries = 0, scrolls = 0, maxScrolls = 100;

        driver.navigate().to(catURL);

        // scroll at the end
        while (scrolls < maxScrolls) {

            // scroll to bottom of product container
            JavascriptExecutor js = (JavascriptExecutor) driver;
            try {
                WebElement loopElement = driver.findElement(By.xpath(locators.get("productsection")));  //scroll
                js.executeScript("arguments[0].scrollIntoView(false);", loopElement);

            } catch (NoSuchElementException e) {
                System.out.println("!!! Product container Element not found in DOM");
                break;
            }

            // sleep
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // track total products loaded in current scroll
            int currProducts;
            try {
                currProducts = driver.findElements(By.xpath(locators.get("productitem"))).size();
            } catch (NoSuchElementException e) {
                System.out.println("!!! ProductItemElement not found in DOM");
                break;
            }

            // scroll by 10 for lazy loading check if product container cannot be loaded
            // more
            if (currProducts == prevProductCount) {
                js.executeScript("window.scrollBy(0, 10);");
                sameCountTries++;
                if (sameCountTries >= 5)
                    break;
            } else {
                sameCountTries = 0;
            }
            // update
            prevProductCount = currProducts;
            scrolls++;

        }

        // if total products are 0 then is failed scrapping
        Assert.assertNotEquals(prevProductCount, 0);

        // extract the product data
        List<WebElement> products = driver.findElements(By.xpath(locators.get("productitem")));

        System.out.println("===> Total products for("+ subcat + ")category are: " + products.size());

        for (int i = 0; i < products.size(); i++) {
            System.out.println("===> getting "+ i + " th prod");
            try {

                WebElement currproduct = products.get(i);
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

                // ---- name ----
                String name = null;
                try {
                    String xp = locators.get("name");
                    if (xp == null || xp.trim().isEmpty()) {
                        System.out.println("[name] Empty XPath — skipping");
                    } else {
                        name = currproduct.findElement(By.xpath(xp)).getText().trim();
                    }
                } catch (Exception e) {
                    System.out.println("!!! [name] XPATH=" + locators.get("name") + " | " + e.getClass().getSimpleName() + " - " + e.getMessage());
                }

                // ---- url ----
                String url = null;
                try {
                    String urlXp = locators.get("url");
                    if (urlXp == null || urlXp.trim().isEmpty()) {
                        System.out.println("[url] Empty XPath — skipping");
                    } else if (!urlXp.equalsIgnoreCase("routertype")) {
                        url = currproduct.findElement(By.xpath(urlXp)).getAttribute("href");
                    } else {
                        // Router-type navigation and back (uses image/title)
                        String clickXp = locators.get("imageurl");
                        if (clickXp == null || clickXp.trim().isEmpty()) {
                            System.out.println("[url/routertype] Empty click XPath (imageurl) — cannot navigate");
                        } else {
                            String listingUrl = driver.getCurrentUrl();
                            try {
                                WebElement clickTarget = currproduct.findElement(By.xpath(clickXp)); // or title
                                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", clickTarget);
                                clickTarget.click();
                                wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(listingUrl)));
                                url = driver.getCurrentUrl();
                            } catch (Exception e) {
                                System.out.println("!!! [url/routertype] click failed XPATH=" + clickXp
                                        + " | " + e.getClass().getSimpleName() + " - " + e.getMessage());
                            } finally {
                                try {
                                    driver.navigate().back();
                                    wait.until(ExpectedConditions.urlToBe(listingUrl));
                                } catch (Exception backEx) {
                                    System.out.println("!!! [navigation] back to listing failed | "
                                            + backEx.getClass().getSimpleName() + " - " + backEx.getMessage());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("!!! [url] XPATH=" + locators.get("url") + " | " + e.getClass().getSimpleName() + " - " + e.getMessage());
                }
                
                //as dom is refreshed
                if (locators.get("url").equalsIgnoreCase("routertype")){
                    Thread.sleep(2000);
                    products = driver.findElements(By.xpath(locators.get("productitem")));
                    currproduct = products.get(i);
                }

                // Essential-only skip (product unusable if name or url missing)
                if (name == null || url == null) {
                    System.out.println("Skipping product #" + (i + 1) + " — missing essential field(s): "
                            + (name == null ? "name " : "") + (url == null ? "url" : ""));
                    System.out.println("---------------------------");
                    continue;
                }

                // ---- id ----
                String id = storename + Math.abs(url.hashCode());

                // ---- price ----
                int price = 0;
                try {
                    String priceXp = locators.get("price");
                    if (priceXp == null || priceXp.trim().isEmpty()) {
                        System.out.println("[price] Empty XPath — skipping");
                    } else {
                        String pr = currproduct.findElement(By.xpath(priceXp)).getText().trim();
                        try {
                            // Minimal: keep digits + dot; adjust as per your formatting needs
                            price = (int) Math.round(Double.parseDouble(pr.replaceAll("[^0-9.]", "")));
                        } catch (NumberFormatException nfe) {
                            System.out.println("!!! [price] parse failed for value: '" + pr + "' | " + nfe.getClass().getSimpleName());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("!!! [price] XPATH=" + locators.get("price") + " | " + e.getClass().getSimpleName() + " - " + e.getMessage());
                }

                // ---- discount_percent ----
                int discount_percent = 0;
                try {
                    String dp = locators.get("discount_percent");
                    if (dp == null || dp.trim().isEmpty()) {
                        System.out.println("[discount_percent] Empty XPath — skipping");
                    } else {
                        String disp = currproduct.findElement(By.xpath(dp)).getText().trim();
                        try {
                            // Minimal: keep digits + dot; adjust as per your formatting needs
                            discount_percent = Math.round(Integer.parseInt(disp.replaceAll("[^0-9.]", "")));
                        } catch (NumberFormatException nfe) {
                            System.out.println("!!! [discount_percent] parse failed for value: '" + disp + "' | " + nfe.getClass().getSimpleName());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("!!! [discount_percent] XPATH=" + locators.get("discount_percent") + " | " + e.getClass().getSimpleName() + " - " + e.getMessage());
                }

                // ---- imageurl ----
                String imageurl = "";
                try {
                    String imgXp = locators.get("imageurl");
                    if (imgXp == null || imgXp.trim().isEmpty()) {
                        System.out.println("[imageurl] Empty XPath — skipping");
                    } else {
                        imageurl = currproduct.findElement(By.xpath(imgXp)).getAttribute("src");
                        if (imageurl == null) imageurl = "";
                    }
                } catch (Exception e) {
                    System.out.println("!!! [imageurl] XPATH=" + locators.get("imageurl") + " | " + e.getClass().getSimpleName() + " - " + e.getMessage());
                }

                // ---- description ----
                String des = "";
                try {
                    String xp = locators.get("description");
                    if (xp == null || xp.trim().isEmpty()) {
                        System.out.println("[description] Empty XPath — skipping");
                    } else {
                        des = currproduct.findElement(By.xpath(xp)).getText().trim();
                    }
                } catch (Exception e) {
                    System.out.println("!!! [description] XPATH=" + locators.get("description") + " | " + e.getClass().getSimpleName() + " - " + e.getMessage());
                }

                
                // ---- rating ----
                float rating = 0.0f;
                try {
                    String xp = locators.get("rating");
                    if (xp == null || xp.trim().isEmpty()) {
                        System.out.println("[rating] Empty XPath — skipping");
                    } else {
                        String rt = currproduct.findElement(By.xpath(xp)).getText().trim();
                        try {
                            rating = Float.parseFloat(rt.replaceAll("[^0-9.]", ""));
                        } catch (NumberFormatException nfe) {
                            // keep default 0.0
                        }
                    }
                } catch (Exception e) {
                    System.out.println("!!! [rating] XPATH=" + locators.get("rating") + " | " + e.getClass().getSimpleName() + " - " + e.getMessage());
                }

                // ---- availability ----
                boolean isAvailable = false;
                try {
                    String idf = locators.get("availablity_idf");
                    if (idf == null || idf.trim().isEmpty()) {
                        System.out.println("!!! [availability] Empty identifier — skipping");
                    } else {
                        isAvailable = !currproduct.findElements(By.xpath(
                                ".//descendant::*[contains(text(), '" + idf + "')]"
                        )).isEmpty();
                    }
                } catch (Exception e) {
                    System.out.println("!!! [availability] evaluation failed | " + e.getClass().getSimpleName() + " - " + e.getMessage());
                }



                //display
                Product p = new Product(id, name, url, imageurl, maincat, subcat, price ,discount_percent, des, rating, isAvailable);
                productList.add(p);
                System.out.println("DATA >>>");
                System.out.println(p);
                System.out.println("=====================================");

            } catch (NoSuchElementException e) {
                System.out.println("===> Product structure mismatch: " + e);
            } catch (Exception e) {
                System.out.println("!!!" + e.toString());
            }
        }

    }

    public static void main(String[] args) {
        demo d = new demo();
        d.scrapProdData();
    }
}
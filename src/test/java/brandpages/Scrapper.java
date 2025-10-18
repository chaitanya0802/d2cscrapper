package brandpages;

import java.util.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import io.github.bonigarcia.wdm.WebDriverManager;
import utils.ConfigReader;
import utils.Product;
import java.time.Duration;

//  !!! = issue

public class Scrapper {

    static WebDriver driver;

    public static void main(String[] args) {
        // driver config
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();

        Scrapper scraper = new Scrapper();
        scraper.scrapController();
    }

    @Test
    public void scrapController() {

        if (Boolean.parseBoolean(ConfigReader.getProperty("product"))) {
            scrapProdData();
        }

        if (Boolean.parseBoolean(ConfigReader.getProperty("offer"))) {
            scrapOfferData();
        }
    }

    //scrap products data
    public static void scrapProdData() {

        try {

            String jsonPath = ConfigReader.getProperty("json_path");
            String brandList = ConfigReader.getProperty("brands_to_scrape").trim().toLowerCase();

            //brands.json
            Map<String, BrandConfig> brandDataMap = JsonReader.loadBrandConfigs(jsonPath);

            //Determine which brands to scrape
            Set<String> allBrands = brandDataMap.keySet();
            List<String> brandsToScrape = new ArrayList<>();

            //scrap all brands
            if (brandList.equals("all")) {
                brandsToScrape.addAll(allBrands);
                System.out.println("Config says: Scrape ALL brands (" + allBrands.size() + ")");
            }
            //scrap only defined in prop file
            else {
                for (String b : brandList.split(",")) {
                    String brand = b.trim().toLowerCase();
                    if (allBrands.contains(brand)) {
                        brandsToScrape.add(brand);
                    } else {
                        System.out.println("!!! Brand '" + brand + "' not found in JSON file. Skipping.");
                    }
                }
                System.out.println("Config says: scrap: " + brandsToScrape);
            }

            if (brandsToScrape.isEmpty()) {
                System.out.println("!!! No valid brands found to scrape. Exiting.");
                return;
            }

            //loop through each selected brand
            for (String brand : brandsToScrape) {
                BrandConfig cfg = brandDataMap.get(brand);

                System.out.println("===> Getting data for: " + brand);

                for (Map.Entry<String, String> entry : cfg.subcategories.entrySet()) {
                    String subcatName = entry.getKey();     //may have + in it
                    String url = entry.getValue();
                    String maincat = cfg.maincategory;

                    System.out.println("==> Getting data for Category: "+ maincat + " => " + subcatName);

                    //execute scrapping for category
                    scrapCategoryData(maincat, subcatName, url, cfg.locators, brand);
                }
            }

        } catch (Exception e) {
            System.out.println("!!! Failed to start scraping: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void scrapCategoryData(String maincat, String subcat, String catURL, Map<String, String> locators, String brand) {
        List<Product> productList = new ArrayList<>();
        driver.navigate().to(catURL);
        System.out.println("Navigated to: " + catURL);

//------------------------------------------------
//scrolling

        int prevProductCount = 0, sameCountTries = 0, scrolls = 0, maxScrolls = 1000;

        while (scrolls < maxScrolls) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String productcardsectionType = locators.get("productcardsection");


            try {
                if (productcardsectionType.equalsIgnoreCase("windowtype")) {
                    js.executeScript("window.scrollBy(0, 300);");
                }
                // scroll to bottom of product container
                else {
                    WebElement loopElement = driver.findElement(By.xpath(locators.get("productcardsection")));
                    js.executeScript("arguments[0].scrollIntoView(false);", loopElement);
                }

                System.out.println("Scrolling...");


            } catch (NoSuchElementException e) {
                System.out.println("!!! Product container Element not found in DOM: " + e.getClass().getSimpleName() + " - " + e.getMessage());
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
                currProducts = driver.findElements(By.xpath(locators.get("productcard"))).size();
            } catch (NoSuchElementException e) {
                System.out.println("!!! productcardElement not found in DOM");
                break;
            }

            //check for termination
            if (currProducts == prevProductCount) {
                //only do tiny scrolls for non-windowtype sites
                if (!productcardsectionType.equalsIgnoreCase("windowtype")) {
                    js.executeScript("window.scrollBy(0, 10);");
                }
                sameCountTries++;
                int limit = productcardsectionType.equalsIgnoreCase("windowtype") ? 10 : 5;

                System.out.println("sameCountTries: " + sameCountTries);
                if (sameCountTries >= limit) {
                    System.out.println(">>> Breaking scrolling as sameCountTries >= " + limit);
                    break;
                }
            } else {
                sameCountTries = 0;
            }

            // update
            prevProductCount = currProducts;
            scrolls++;
        }

//------------------------------------------------
//extracting data

        // if total products are 0 then is failed scrapping
        Assert.assertNotEquals(prevProductCount, 0);

        List<WebElement> products = driver.findElements(By.xpath(locators.get("productcard")));
        System.out.println("==> Total products for (" + subcat + ") category are: " + products.size());

        for (int i = 0; i < products.size(); i++) {
            System.out.println("===> getting " + i + " th prod");
            try {

                WebElement currproduct = products.get(i);
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

                //name
                String name = null;
                try {
                    String xp = locators.get("name");
                    name = currproduct.findElement(By.xpath(xp)).getText().trim();
                }
                catch (NoSuchElementException exc) {
                    System.out.println("!!! Product Name not found");
                }catch (Exception e) {
                    System.out.println("!!! [name] XPATH=" + locators.get("name") + " | " + e.getClass().getSimpleName()
                            + " - " + e.getMessage());
                }

                //url
                String url = null;
                try {
                    String urlXp = locators.get("url");

                    if (!urlXp.equalsIgnoreCase("routertype")) {
                        url = currproduct.findElement(By.xpath(urlXp)).getAttribute("href");
                    } else {
                        // Router-type navigation and back (uses image/title)
                        String clickXp = locators.get("imageurl");
                        String listingUrl = driver.getCurrentUrl();
                        try {
                            WebElement clickTarget = currproduct.findElement(By.xpath(clickXp)); // image
                            ((JavascriptExecutor) driver)
                                    .executeScript("arguments[0].scrollIntoView({block:'center'});", clickTarget);
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
                catch (NoSuchElementException exc) {
                    System.out.println("!!! Product Url not found");

                }catch (Exception e) {
                    System.out.println("!!! [url] XPATH=" + locators.get("url") + " | " + e.getClass().getSimpleName()
                            + " - " + e.getMessage());
                }

                // if dom is refreshed due to routertype
                if (locators.get("url").equalsIgnoreCase("routertype")) {
                    Thread.sleep(2000);
                    products = driver.findElements(By.xpath(locators.get("productcard")));
                    currproduct = products.get(i);
                }

                // Essential-only skip (product unusable if name or url missing)
                if (name == null || url == null) {
                    System.out.println("Skipping product #" + (i + 1) + " — missing essential field(s): "
                            + (name == null ? "name " : "") + (url == null ? "url" : ""));
                    System.out.println("-------------------------------------------");
                    continue;
                }

                //id
                String id = brand + Math.abs(url.hashCode());

                //price
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
                            System.out.println("!!! [price] parse failed for value: '" + pr + "' | "
                                    + nfe.getClass().getSimpleName());
                        }
                    }
                }
                catch (NoSuchElementException ex){
                    System.out.println("!!! Price not found for: " + id);
                }
                catch (Exception e) {
                    System.out.println("!!! [price] XPATH=" + locators.get("price") + " | "
                            + e.getClass().getSimpleName() + " - " + e.getMessage());
                }

                if (price == 0) {
                    System.out.println("Skipping product #" + (i + 1) + " — as price is 0 ");
                    System.out.println("-------------------------------------------");
                    continue;
                }

                //discount_percent
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
                            System.out.println("!!! [discount_percent] parse failed for value: '" + disp + "' | "
                                    + nfe.getClass().getSimpleName());
                        }
                    }
                }catch (NoSuchElementException exc) {
                    System.out.println("Discount not found for: " + id);
                }catch (Exception e) {
                    System.out.println("!!! [discount_percent] XPATH=" + locators.get("discount_percent") + " | "
                            + e.getClass().getSimpleName() + " - " + e.getMessage());
                }

                //imageurl
                String imageurl = "";
                try {
                    String imgXp = locators.get("imageurl");
                    if (imgXp == null || imgXp.trim().isEmpty()) {
                        System.out.println("[imageurl] Empty XPath — skipping");
                    } else {
                        imageurl = currproduct.findElement(By.xpath(imgXp)).getAttribute("src");
                        if (imageurl == null)
                            imageurl = "";
                    }
                } catch (NoSuchElementException exc) {
                    System.out.println("!!!  Image not found for: " + id);
                }catch (Exception e) {
                    System.out.println("!!! [imageurl] XPATH=" + locators.get("imageurl") + " | "
                            + e.getClass().getSimpleName() + " - " + e.getMessage());
                }

                //description
                String des = "";
                try {
                    String xp = locators.get("description");
                    if (xp == null || xp.trim().isEmpty()) {
                        System.out.println("[description] Empty XPath — skipping");
                    } else {
                        des = currproduct.findElement(By.xpath(xp)).getText().trim();
                    }
                } catch (NoSuchElementException exc) {
                    System.out.println("Description not found for: " + id);
                }catch (Exception e) {
                    System.out.println("!!! [description] XPATH=" + locators.get("description") + " | "
                            + e.getClass().getSimpleName() + " - " + e.getMessage());
                }

                //rating
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
                }
                catch (NoSuchElementException exc) {
                    System.out.println("Rating not found for: " + id);
                }catch (Exception e) {
                    System.out.println("!!! [rating] XPATH=" + locators.get("rating") + " | "
                            + e.getClass().getSimpleName() + " - " + e.getMessage());
                }

                // availability
                boolean isAvailable = false;
                try {
                    String idf = locators.get("availability_idf");
                    if (idf == null || idf.trim().isEmpty()) {
                        System.out.println("!!! [availability] Empty identifier — skipping");
                    } else {
                        isAvailable = !currproduct.findElements(By.xpath(
                                ".//descendant::*[contains(text(), '" + idf + "')]")).isEmpty();
                    }
                } catch (Exception e) {
                    System.out.println("!!! [availability] evaluation failed | " + e.getClass().getSimpleName() + " - "
                            + e.getMessage());
                }

                //display
                Product p = new Product(id, name, url, imageurl, maincat, subcat, price, discount_percent, des, rating,
                        isAvailable);
                productList.add(p);
                System.out.println("=== DATA");
                System.out.println(p);
                System.out.println("===============================================================");

            } catch (Exception e) {
                System.out.println("!!!" + e);
            }
        }
    }


    //offer scraping
    public void scrapOfferData() {
        //implement later
    }
}
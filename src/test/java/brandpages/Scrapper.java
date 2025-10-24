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
import utils.Offer;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

//  !!! = issue

public class Scrapper {

    static WebDriver driver;
    static Properties cat_prop;

    public static void main(String[] args) {

        Scrapper scraper = new Scrapper();
        scraper.scrapController();
    }

    @Test
    public void scrapController() {
        // driver config
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();

        if (Boolean.parseBoolean(ConfigReader.getProperty("product"))) {
            scrapProdData();
        }

        if (Boolean.parseBoolean(ConfigReader.getProperty("offer"))) {
            scrapOfferData();
        }
    }

    // scrap products data
    public static void scrapProdData() {

        try {

            String jsonPath = ConfigReader.getProperty("products_json_path");
            String brandList = ConfigReader.getProperty("brands_to_scrape").trim().toLowerCase();

            // read json
            Map<String, ProductScrapConfig> brandDataMap = ProductScrapJsonReader.loadBrandConfigs(jsonPath);

            // Determine which brands to scrape
            Set<String> allBrands = brandDataMap.keySet();
            List<String> brandsToScrape = new ArrayList<>();

            // scrap all brands
            if (brandList.equals("all")) {
                brandsToScrape.addAll(allBrands);
                System.out.println("Config says: Scrape ALL brands (" + allBrands.size() + ")");
            }
            // scrap only defined in prop file
            else {
                for (String b : brandList.split(",")) {
                    String brand = b.trim().toLowerCase();
                    if (allBrands.contains(brand)) {
                        brandsToScrape.add(brand);
                    } else {
                        System.out.println("!!! Brand '" + brand + "' not found in JSON file. Skipping.");
                    }
                }
                System.out.println("Config says: scrap=" + brandsToScrape);
            }

            if (brandsToScrape.isEmpty()) {
                System.out.println("!!! No valid brands found to scrape. Exiting.");
                return;
            }

            int overallProgress = 0;
            int totalBrands = brandsToScrape.size();

            // loop through each selected brand
            for (String brand : brandsToScrape) {
                ProductScrapConfig cfg = brandDataMap.get(brand);

                System.out.println("===> Getting product data for: " + brand);

                int total_subcat = cfg.subcategories.size();
                int Brandprogress = 0;

                for (Map.Entry<String, String> entry : cfg.subcategories.entrySet()) {

                    String subcatName = entry.getKey(); // may have + in it
                    String url = entry.getValue();
                    String maincat = cfg.maincategory;

                    // overall progress
                    int overallcompletedpercent = (overallProgress * 100) / totalBrands;
                    System.out.println("Progress >>> ");
                    System.out.print("[");
                    for (int i = 0; i < 50; i++) {
                        if (i < overallcompletedpercent / 2) {
                            System.out.print("#");
                        } else {
                            System.out.print(" ");
                        }
                    }
                    System.out.print("] " + overallcompletedpercent + "%" + " Overall");
                    System.out.println(" ");

                    // current brand progress
                    int completedpercent = (Brandprogress * 100) / total_subcat;
                    System.out.print("[");
                    for (int i = 0; i < 50; i++) {
                        if (i < completedpercent / 2) {
                            System.out.print("#");
                        } else {
                            System.out.print(" ");
                        }
                    }
                    System.out.print("] " + completedpercent + "%" + " for: " + brand);
                    System.out.println(" ");

                    System.out.println("\n==> Getting data for: " + maincat + " > " + subcatName);

                    // execute scrapping for category
                    scrapCategoryData(maincat, subcatName, url, cfg.locators, brand, cfg.store_id, cfg.store_name,
                            cfg.store_url);

                    Brandprogress++;
                }

                System.out.println("\n===> Scrapping completed for: " + brand);
                overallProgress++;
            }

        } catch (Exception e) {
            System.out.println("!!! Failed to start scraping: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // scrapping products by catgory
    public static void scrapCategoryData(String maincat, String subcat, String catURL, Map<String, String> locators,
            String brand, int store_id, String store_name, String store_url) {

        List<Product> productList = new ArrayList<>();
        driver.navigate().to(catURL);
        System.out.println("Navigated to: " + catURL);

        // ------------------------------------------------
        // scrolling

        int prevProductCount = 0, sameCountTries = 0, scrolls = 0, maxScrolls = 1500;

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
                System.out.println("!!! Product container Element not found in DOM: " + e.getClass().getSimpleName()
                        + " - " + e.getMessage());
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

            // check for termination
            if (currProducts == prevProductCount) {
                // only do tiny scrolls for non-windowtype sites
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

        // ------------------------------------------------
        // extracting data

        // if total products are 0 then is failed scrapping
        Assert.assertNotEquals(prevProductCount, 0);

        List<WebElement> products = driver.findElements(By.xpath(locators.get("productcard")));
        System.out.println("==> Total products for (" + subcat + ") category are: " + products.size());

        for (int i = 0; i < products.size(); i++) {
            System.out.println("===> getting " + i + " th prod");
            try {

                WebElement currproduct = products.get(i);
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

                // name
                String name = null;
                try {
                    String xp = locators.get("name");
                    name = currproduct.findElement(By.xpath(xp)).getText().trim();
                } catch (NoSuchElementException exc) {
                    System.out.println("!!! Product Name not found");
                } catch (Exception e) {
                    System.out.println("!!! [name] XPATH=" + locators.get("name") + " | " + e.getClass().getSimpleName()
                            + " - " + e.getMessage());
                }

                // url
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
                } catch (NoSuchElementException exc) {
                    System.out.println("!!! Product Url not found");

                } catch (Exception e) {
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

                // id
                String id = brand.replaceAll(" ", "") + Math.abs(url.hashCode());

                // price
                float price = 0;
                try {
                    String priceXp = locators.get("price");
                    if (priceXp == null || priceXp.trim().isEmpty()) {
                        System.out.println("[price] Empty XPath — skipping");
                    } else {
                        String pr = currproduct.findElement(By.xpath(priceXp)).getText().trim();

                        Matcher m = Pattern.compile("(\\d+(?:[.,]\\d+)?)").matcher(pr);
                        if (m.find()) {
                            String num = m.group(1).replace(",", "");
                            price = (float) Math.round(Double.parseDouble(num));
                        }

                    }
                } catch (NoSuchElementException ex) {
                    System.out.println("!!! Price not found for: " + id);
                } catch (Exception e) {
                    System.out.println("!!! [price] XPATH=" + locators.get("price") + " | "
                            + e.getClass().getSimpleName() + " - " + e.getMessage());
                }

                if (price == 0) {
                    System.out.println("Skipping product #" + (i + 1) + " — as price is 0 ");
                    System.out.println("-------------------------------------------");
                    continue;
                }

                // discount_percent
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
                } catch (NoSuchElementException exc) {
                    System.out.println("Discount not found for: " + id);
                } catch (Exception e) {
                    System.out.println("!!! [discount_percent] XPATH=" + locators.get("discount_percent") + " | "
                            + e.getClass().getSimpleName() + " - " + e.getMessage());
                }

                // imageurl
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
                } catch (Exception e) {
                    System.out.println("!!! [imageurl] XPATH=" + locators.get("imageurl") + " | "
                            + e.getClass().getSimpleName() + " - " + e.getMessage());
                }

                // description
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
                } catch (Exception e) {
                    System.out.println("!!! [description] XPATH=" + locators.get("description") + " | "
                            + e.getClass().getSimpleName() + " - " + e.getMessage());
                }

                // rating
                float rating = 0.0f;
                try {
                    String xp = locators.get("rating");
                    if (xp == null || xp.trim().isEmpty()) {
                        System.out.println("[rating] Empty XPath — skipping");
                    }
                    // get rating from elements attribute
                    else if (!locators.get("rating_type").equalsIgnoreCase("text")) {
                        String rt = currproduct.findElement(By.xpath(xp)).getAttribute(locators.get("rating_type"));
                        try {
                            rating = Float.parseFloat(rt.replaceAll("[^0-9.]", ""));
                        } catch (NumberFormatException nfe) {
                            // keep default 0.0
                        }
                    }
                    // get text
                    else {
                        String rt = currproduct.findElement(By.xpath(xp)).getText().trim();
                        try {
                            rating = Float.parseFloat(rt.replaceAll("[^0-9.]", ""));
                        } catch (NumberFormatException nfe) {
                            // keep default 0.0
                        }
                    }
                } catch (NoSuchElementException exc) {
                    System.out.println("Rating not found for: " + id);
                } catch (Exception e) {
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

                // Convert categories to list of Integer
                ArrayList<Integer> cate_ids = CategoryToInt.getListOfCat(maincat, subcat);

                // display
                Product p = new Product(id, name, url, imageurl, cate_ids, price, discount_percent, des, rating,
                        isAvailable, store_id, store_name, store_url);
                productList.add(p);

                System.out.println("=== DATA");
                System.out.println(p);
                System.out.println("===============================================================");

            } catch (Exception e) {
                System.out.println("!!!" + e);
            }
        }
    }

    // ==============================================================================================

    // offer scraping
    public void scrapOfferData() {

        try {
            String offerjsonPath = ConfigReader.getProperty("offers_json_path");
            String brandList = ConfigReader.getProperty("brands_to_scrape").trim().toLowerCase();

            // read json
            Map<String, OfferScrapConfig> brandDataMap = OfferScrapJsonReader.loadBrandConfigs(offerjsonPath);

            // Determine which brands to scrape
            Set<String> allBrands = brandDataMap.keySet();
            List<String> brandsToScrape = new ArrayList<>();

            // scrap all brands
            if (brandList.equals("all")) {
                brandsToScrape.addAll(allBrands);
                System.out.println("Config says: Scrape ALL brands (" + allBrands.size() + ")");
            }
            // scrap only defined in prop file
            else {
                for (String b : brandList.split(",")) {
                    String brand = b.trim().toLowerCase();
                    if (allBrands.contains(brand)) {
                        brandsToScrape.add(brand);
                    } else {
                        System.out.println("!!! Brand '" + brand + "' not found in JSON file. Skipping.");
                    }
                }
                System.out.println("Config says: scrap=" + brandsToScrape);
            }

            if (brandsToScrape.isEmpty()) {
                System.out.println("!!! No valid brands found to scrape. Exiting.");
                return;
            }

            int overallProgress = 0;
            int totalBrands = brandsToScrape.size();

            // loop through each selected brand
            for (String brand : brandsToScrape) {
                OfferScrapConfig cfg = brandDataMap.get(brand);

                System.out.println("===> Getting offer data for: " + brand);

                // int totalpages = cfg.offer_page_url.size();
                for (String page : cfg.offer_page_url) {
                    // overall progress
                    int overallcompletedpercent = (overallProgress * 100) / totalBrands;
                    System.out.println("Progress >>> ");
                    System.out.print("[");
                    for (int i = 0; i < 50; i++) {
                        if (i < overallcompletedpercent / 2) {
                            System.out.print("#");
                        } else {
                            System.out.print(" ");
                        }
                    }
                    System.out.print("] " + overallcompletedpercent + "%" + " Overall");
                    System.out.println(" ");

                    // for every url in offer_page_url
                    scrapPageOfferData(page, cfg.maincategory, cfg.locators, cfg.store_id, cfg.store_name,
                            cfg.store_url, brand);

                }

                System.out.println("\n===> Scrapping Offers completed for: " + brand);

                overallProgress++;
            }

        } catch (Exception e) {
            System.out.println("!!! Failed to start scraping: " + e.getMessage());
            e.printStackTrace();
        }

    }

    // scrap offer data page-wise
    public static void scrapPageOfferData(String pageurl, String maincat, Map<String, String> locators,
            int store_id, String store_name, String store_url, String brand) {

        List<Offer> offerList = new ArrayList<>();
        driver.navigate().to(pageurl);
        System.out.println("Navigated to: " + pageurl);

        int total_offers_found = driver.findElements(By.xpath(locators.get("offercards"))).size();
        System.out.println("Offers found for " + store_name + ": " + total_offers_found);

        if (total_offers_found == 0) {
            System.out.println("!!! Skipping as no offers found");
            return;
        }

        List<WebElement> offerelements = driver.findElements(By.xpath(locators.get("offercards")));
        String offerlink = "", offerimage = "", offername = "", offerid = "";

        for (int i = 0; i < total_offers_found; i++) {
            WebElement e = offerelements.get(i);

            try {
                offerlink = "";

                //offerlink
                try {
                    offerlink = e.getAttribute("href");

                } catch (NoSuchElementException exc) {
                    System.out.println("!!! Offer URL not found");
                } catch (Exception ex) {
                    System.out.println(
                            "!!! [url] XPATH=" + locators.get("offerurl") + " | " + ex.getClass().getSimpleName()
                                    + " - " + ex.getMessage());
                }

                //check
                if (offerlink == null || offerlink.isEmpty()) {
                    System.out.println("No offer URL found, Skipping...");
                    continue;
                }

                //offername
                String lastSegment = offerlink.substring(offerlink.lastIndexOf('/') + 1);
                String[] words = lastSegment.split("-");
                StringBuilder formatted = new StringBuilder();
                for (String word : words) {
                    if (!word.isEmpty()) {
                        formatted.append(Character.toUpperCase(word.charAt(0)))
                                .append(word.substring(1)).append(" ");
                    }
                }
                offername = formatted.toString().trim().replace(".html", "").trim();

                //offer img
                try {
                    String imgurlXp = locators.get("offerimage");
                    
                    if(e.findElement(By.xpath(imgurlXp)).getAttribute("src") == null) continue;

                    offerimage = e.findElement(By.xpath(imgurlXp)).getAttribute("src");
                } catch (NoSuchElementException exc) {
                    System.out.println("!!! Offer image URL not found");
                } catch (Exception ex) {
                    System.out.println(
                            "!!! [img] XPATH=" + locators.get("offerimage") + " | " + ex.getClass().getSimpleName()
                                    + " - " + ex.getMessage());
                }

                if (offerimage == null || offerimage.isEmpty()) {
                    System.out.println("No offerimage URL found, Skipping...");
                    continue;
                }

                //offer id
                offerid = "ofr" + brand.replaceAll(" ", "") + Math.abs(offerlink.hashCode());

                //offer category
                try (FileInputStream fis = new FileInputStream("src/test/resources/category_ids.properties")) {
                    cat_prop = new Properties();
                    cat_prop.load(fis);
                } catch (FileNotFoundException exc) {
                    System.out.println("File not found: " + exc);
                } catch (IOException ioe) {
                    System.out.println(ioe.toString());
                }

                int catid = Integer.parseInt(cat_prop.getProperty(maincat));

                Offer ofr = new Offer(offerid, offerlink, offerimage, offername, catid, store_id, store_name,
                        store_url);
                offerList.add(ofr);

                System.out.println("=== DATA");
                System.out.println(ofr);
                System.out.println("===============================================================");

            } catch (Exception ex) {
                System.out.println("!!! Exception during offer extraction: " + ex.getClass().getSimpleName() + " - "
                        + ex.getMessage());
            }
        }
    }
}
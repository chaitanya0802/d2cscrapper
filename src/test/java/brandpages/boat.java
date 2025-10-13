
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

public class boat {

    String mainCategory = "Electronics";
    //for offer
    List<String> subcat = List.of(
            "AUX",
            "Cables",
            "Car Chargers",
            "Charger",
            "Chargers",
            "Gaming Earbuds",
            "Gaming Headphones",
            "Headphones",
            "Neckbands",
            "Party Speakers",
            "Power Banks",
            "Smart Watch Cable",
            "Smart Watches"
    );

    String storename = "boat";
    String baseUrl = "https://www.boat-lifestyle.com";

    static WebDriver driver;

    public boat() {
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
            driver.get("https://www.boat-lifestyle.com/collections/all-products");
            driver.manage().window().maximize();

            //to store query url
            Map<String, String> map = new HashMap<>();        //cat, query

            int total_cat = driver.findElements(By.xpath("//*[@id=\"facet-filter-filter.p.m.custom.category\"]/div/div/div[2]/input")).size();

            //create map with category and url
            for (int i = 1; i <= total_cat; i++) {
                WebElement cat_ele = driver.findElement(By.xpath("//*[@id=\"facet-filter-filter.p.m.custom.category\"]/div/div[ " + i + "]/div[2]/input"));

                String[] category = cat_ele.getAttribute("value").split(" ");

                String appendval = String.join("+", category);

                map.put(cat_ele.getAttribute("value"), "?sort_by=manual&filter.p.m.custom.category=" + appendval);
            }

            for (Map.Entry<String, String> e : map.entrySet()) {

                driver.navigate().to("https://www.boat-lifestyle.com/collections/all-products" + e.getValue());
                getProdData(e.getKey());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //getting data
    public void getProdData(String subcat) {
        //to store product data
        List<Product> productList = new ArrayList<>();

        System.out.println("GETTING DATA FOR=== " + subcat);

        JavascriptExecutor js = (JavascriptExecutor) driver;
        int prevProductCount = 0, sameCountTries = 0, scrolls = 0;
        int maxScrolls = 100;

        while (scrolls < maxScrolls) {
            try {
                WebElement loopElement = driver.findElement(By.xpath("//*[@id='facet-main']/product-list"));
                js.executeScript("arguments[0].scrollIntoView(false);", loopElement);
            } catch (NoSuchElementException e) {
                System.out.println("Product container not found.");
                break;
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<WebElement> products = driver.findElements(By.xpath("//*[@id='Huratips_Loop']//product-item"));
            for (int i = prevProductCount; i < products.size(); i++) {

                System.out.println("$$$current product count: " + products.size());

                try {
                    //actual data

                    WebElement link = products.get(i).findElement(By.xpath(".//div/div[2]/div/div/a"));

                    String name = link.getText().trim();
                    String url = link.getAttribute("href");
                    String id = storename + Math.abs(url.hashCode());
                    String imageurl = products.get(i).findElement(By.xpath(".//div/div[1]/div/a/img")).getAttribute("src");
                    String price = products.get(i).findElement(By.xpath(".//div/div[2]/div/div/div[1]/div[1]/span[1]")).getAttribute("data-price").trim();
                    int prodprice = Integer.parseInt(price.replaceAll("[^0-9]", "")) / 100;
                    String des = products.get(i).findElement(By.xpath(".//div/div[2]/div/div/div[1]/div[2]")).getText().trim().replaceAll("[™®]", "").trim();
                    String rt = products.get(i).findElement(By.xpath(".//div/div[2]/div/span/div/div")).getText().trim();
                    Float rating = (Float) Float.parseFloat(rt);

                    boolean isAvailable = !products.get(i).findElements(By.xpath(
                    ".//descendant::*[contains(text(), 'Add to cart')]"
                    )).isEmpty();

                    Product p = new Product(id, name, url, imageurl, mainCategory, subcat, prodprice, des, rating, isAvailable);
                    productList.add(p);

                    System.out.println(p);
                    System.out.println("===============");


                } catch (NoSuchElementException e) {
                    System.out.println("Product structure mismatch");
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }

            if (products.size() == prevProductCount) {
                js.executeScript("window.scrollBy(0, 10);");
                sameCountTries++;
                if (sameCountTries >= 5) break;
            } else {
                sameCountTries = 0;
            }

            prevProductCount = products.size();
            scrolls++;

            boolean res;
            try {
                res = ProductPoster.postProducts(productList, storename);
                Assert.assertTrue(res, "Product posting failed for: " + storename + "->" + subcat);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //========================== offer

    public void scrapOfferData() {

        List<Offer> offerList = new ArrayList<>();

        driver.get("https://www.boat-lifestyle.com");
        driver.manage().window().maximize();

        int total_offers = driver.findElements(By.xpath("//*[@id=\"MultiCarousel_template--16905581428834__e4e83c48-4e49-4d76-a45b-e25f3061707b1\"]/div/div")).size();

        for (int i = 1; i <= total_offers; i++) {
            WebElement of = driver.findElement(By.xpath("//*[@id=\"MultiCarousel_template--16905581428834__e4e83c48-4e49-4d76-a45b-e25f3061707b1\"]/div/div[" + i + "]"));

            String offerlink = of.findElement(By.xpath(".//a")).getAttribute("href");
            if (offerlink.startsWith("/")) offerlink = baseUrl + offerlink;

            String[] parts = offerlink.split("/");
            String offername = parts[parts.length - 1].replace("-", " ");

            String id = storename + "ofr" + Math.abs(offerlink.hashCode());

            String image = of.findElement(By.xpath(".//a/picture/img")).getAttribute("src");

            System.out.println(offerlink + "\n" + offername + "\n" + id + "\n" + image);
            System.out.println("=============");

            offerList.add(new Offer(id, offerlink, image, offername, mainCategory, subcat));

        }

        boolean res;
        try {
            res = OfferPoster.postOffers(offerList, storename);
            Assert.assertTrue(res, "Offer posting failed for: " + storename);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        boat b = new boat();
        b.scrapProdData();      
    }
}

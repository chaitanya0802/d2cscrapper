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

public class demo {
    
    String storename = "Boat";
    String maincat = "Electronics";

    String categoryUrl = "https://www.boat-lifestyle.com/collections/true-wireless-earbuds";
    String productContainerElement = "//*[@id='facet-main']/product-list";
    String productItemElement = "//*[@id='Huratips_Loop']//product-item";
    String productNameElement = "";


    @Test
    public void scrapController() {
        if (Boolean.parseBoolean(ConfigReader.getProperty("product"))) scrapProdData();

        // if (Boolean.parseBoolean(ConfigReader.getProperty("offer"))) scrapOfferData();

    }


    public void scrapProdData() {

        int prevProductCount = 0, sameCountTries = 0, scrolls = 0, maxScrolls = 100;

        // driver config
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.get(categoryUrl);

        //scroll at the end
        while(scrolls < maxScrolls) {

            // scroll to bottom of product container
            JavascriptExecutor js = (JavascriptExecutor) driver;
            try {
                WebElement loopElement = driver.findElement(By.xpath(productContainerElement));
                js.executeScript("arguments[0].scrollIntoView(false);", loopElement);

            } catch (NoSuchElementException e) {
                System.out.println("Product container Element not found in DOM");
                break;
            }
           
            //sleep
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // track total products loaded in current scroll
            int currProducts;
            try{
                currProducts = driver.findElements(By.xpath(productItemElement)).size();
            } catch (NoSuchElementException e) {
                System.out.println("ProductItemElement not found in DOM");
                break;
            }

            // scroll by 10 for lazy loading check if product container cannot be loaded more
            if (currProducts == prevProductCount) {
                js.executeScript("window.scrollBy(0, 10);");
                sameCountTries++;
                if (sameCountTries >= 5) break;
            } else {
                sameCountTries = 0;
            }
            //update
            prevProductCount = currProducts;
            scrolls++;

        }

        System.out.println("Total ProductS Found: " + prevProductCount);

        //if total products are 0 then is failed scrapping
        Assert.assertNotEquals(prevProductCount, 0);


        //extract the product data
        List<WebElement> products = driver.findElements(By.xpath(productItemElement));

        for(int i=0; i<products.size(); i++){
            try{

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

                    WebElement container = products.get(i).findElement(By.xpath(".//div/div[2]/div/div/div[2]"));
                    String tag = container.findElement(By.xpath("./*")).getTagName();
                    boolean isAvailable = tag.equals("form");  // true if form → available

                    Product p = new Product(id, name, url, imageurl, maincat, subcat, prodprice, des, rating, isAvailable);
                    productList.add(p);

                    System.out.println(p);
                    System.out.println("===============");

            } catch (NoSuchElementException e) {
                System.out.println("Product Element not found in DOM");
                break;
            }
        }

    }

    public static void main(String[] args) {
        demo d = new demo();
        d.scrapProdData();
    }
}
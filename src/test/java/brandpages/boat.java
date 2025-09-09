
package brandpages;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;
import utils.Product;
import utils.ProductPoster;

public class boat {

    static WebDriver driver; 
    //to store product data
    List<Product> productList = new ArrayList<>();
    String mainCategory = "Electronics";
    String storename = "boat";

    //to store query url
    Map<String, String> map = new HashMap<>();		//cat, query
    String baseUrl = "https://www.boat-lifestyle.com/collections/all-products";

    public boat() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }

    @Test
    public void scrapData() {
        try {
        	
            driver.get("https://www.boat-lifestyle.com/collections/all-products");
            driver.manage().window().maximize();
            
            int total_cat = driver.findElements(By.xpath("//*[@id=\"facet-filter-filter.p.m.custom.category\"]/div/div/div[2]/input")).size();
            
            //create map with category and url
            for(int i=1; i<=total_cat;i++) {
            	WebElement cat_ele =driver.findElement(By.xpath("//*[@id=\"facet-filter-filter.p.m.custom.category\"]/div/div[ "+ i +"]/div[2]/input"));
            	
            	String[] category = cat_ele.getAttribute("value").split(" ");
            	
            	String appendval = String.join("+", category);
            	
            	map.put(cat_ele.getAttribute("value"), "?sort_by=manual&filter.p.m.custom.category=" + appendval);
            }
            
            for(Map.Entry<String, String> e: map.entrySet()) {

            	driver.navigate().to(baseUrl+e.getValue());
            	getData(e.getKey());
            }
            
            
        }catch(Exception e) {
        	
        }
    }

    //getting data
    public void getData(String subcat) {
    	
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
                try {
                	//actual data
                	
                    WebElement link = products.get(i).findElement(By.xpath(".//div/div[2]/div/div/a"));
                    
                    String name = link.getText().trim();
                    String url = link.getAttribute("href");
                    String id = storename + Math.abs(url.hashCode());
                    String imageurl = products.get(i).findElement(By.xpath(".//div/div[1]/div/a/img")).getAttribute("src");
                    String price = products.get(i).findElement(By.xpath(".//div/div[2]/div/div/div[1]/div[1]/span[1]")).getAttribute("data-price").trim();
                    int prodprice = Integer.parseInt(price.replaceAll("[^0-9]", ""))/100;
                    String des = products.get(i).findElement(By.xpath(".//div/div[2]/div/div/div[1]/div[2]")).getText().trim().replaceAll("[™®]", "").trim();
                    String rt = products.get(i).findElement(By.xpath(".//div/div[2]/div/span/div/div")).getText().trim();
                    Float rating = Float.parseFloat(rt);
                    
                    WebElement container = products.get(i).findElement(By.xpath(".//div/div[2]/div/div/div[2]"));
                    String tag = container.findElement(By.xpath("./*")).getTagName();
                    boolean isAvailable = tag.equals("form");  // true if form → available
                    
                    Product p = new Product(id, name, url, imageurl, mainCategory, subcat, prodprice, des, rating, isAvailable);
                    productList.add(p);
                    
//                    System.out.println(p.toString());
//                    System.out.println("===============");
                    
                    
                    
                } catch (NoSuchElementException e) {
                    System.out.println("Product structure mismatch");
                }
                catch(Exception e) {
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
}

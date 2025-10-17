package brandpages;

import java.util.Map;

public class BrandConfig {
    public Map<String, String> locators;
    public String maincategory;
    public Map<String, String> subcategories;

    public BrandConfig() {}

    public BrandConfig(Map<String, String> locators, String maincategory, Map<String, String> subcategories) {
        this.locators = locators;
        this.maincategory = maincategory;
        this.subcategories = subcategories;
    }

    @Override
    public String toString() {
        return "BrandConfig{" +
                "maincategory='" + maincategory + '\'' +
                ", locators=" + locators +
                ", subcategories=" + subcategories +
                '}';
    }
}
package brandpages;

import java.util.Map;

public class BrandConfig {
    public Map<String, String> locators;
    public String maincategory;
    public Map<String, String> subcategories;

    public int store_id;
    public String store_name;
    public String store_url;

    public BrandConfig() {}

    public BrandConfig(Map<String, String> locators, String maincategory,
     Map<String, String> subcategories, int store_id, String store_name, String store_url) {
        this.locators = locators;
        this.maincategory = maincategory;
        this.subcategories = subcategories;
        this.store_id = store_id;
        this.store_name = store_name;
        this.store_url = store_url;
    }

    @Override
    public String toString() {
        return "BrandConfig{" +
                "maincategory='" + maincategory + '\'' +
                ", locators=" + locators +
                ", subcategories=" + subcategories +
                ", store_id=" + store_id +
                ", store_name=" + store_name +
                ", store_url=" + store_url +
                '}';
    }
}
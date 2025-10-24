package brandpages;

import java.util.List;
import java.util.Map;

public class OfferScrapConfig {
    public Map<String, String> locators;
    public String maincategory;
    public List<String> offer_page_url;

    public int store_id;
    public String store_name;
    public String store_url;

    public OfferScrapConfig() {}

    public OfferScrapConfig(Map<String, String> locators, String maincategory,
                               List<String> offer_page_url, int store_id,
                               String store_name, String store_url) {
        this.locators = locators;
        this.maincategory = maincategory;
        this.offer_page_url = offer_page_url;
        this.store_id = store_id;
        this.store_name = store_name;
        this.store_url = store_url;
    }

    @Override
    public String toString() {
        return "ProductScrapConfig{" +
                "maincategory='" + maincategory + '\'' +
                ", locators=" + locators +
                ", offer_page_url=" + offer_page_url +
                ", store_id=" + store_id +
                ", store_name='" + store_name + '\'' +
                ", store_url='" + store_url + '\'' +
                '}';
    }
}
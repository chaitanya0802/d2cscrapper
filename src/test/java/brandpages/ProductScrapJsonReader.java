package brandpages;

import java.io.FileReader;
import java.util.*;
import com.google.gson.*;

public class ProductScrapJsonReader {

    public static Map<String, ProductScrapConfig> loadBrandConfigs(String jsonPath) {
        Map<String, ProductScrapConfig> brandConfigs = new HashMap<>();

        try (FileReader reader = new FileReader(jsonPath)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject brands = root.getAsJsonObject("brands");

            for (Map.Entry<String, JsonElement> brandEntry : brands.entrySet()) {
                String brandName = brandEntry.getKey().toLowerCase();
                JsonObject brandObj = brandEntry.getValue().getAsJsonObject();

                //Locators
                Map<String, String> locators = new HashMap<>();
                JsonObject locObj = brandObj.getAsJsonObject("locators");
                if (locObj != null) {
                    for (Map.Entry<String, JsonElement> e : locObj.entrySet()) {
                        locators.put(e.getKey(), e.getValue().getAsString());
                    }
                }

                //Main category
                String mainCategory = "";
                if (brandObj.has("maincategory")) {
                    mainCategory = brandObj.get("maincategory").getAsString();
                }

                //Subcategories
                Map<String, String> subcategories = new HashMap<>();
                if (brandObj.has("subcategories")) {
                    JsonObject subObj = brandObj.getAsJsonObject("subcategories");
                    for (Map.Entry<String, JsonElement> e : subObj.entrySet()) {
                        subcategories.put(e.getKey(), e.getValue().getAsString());
                    }
                }

                //store related
                int store_id = 0;
                if (brandObj.has("store_id")) {
                    store_id = brandObj.get("store_id").getAsInt();
                }

                String store_name = "";
                if (brandObj.has("store_name")) {
                    store_name = brandObj.get("store_name").getAsString();
                }

                String store_url = "";
                if (brandObj.has("store_url")) {
                    store_url = brandObj.get("store_url").getAsString();
                }

                //Store in map
                ProductScrapConfig config = new ProductScrapConfig(locators, mainCategory, subcategories, store_id, store_name,store_url);
                brandConfigs.put(brandName, config);
            }

            System.out.println("Loaded " + brandConfigs.size() + " brand configurations from JSON.");

        } catch (Exception e) {
            System.out.println("!!! Error reading JSON: " + e.getMessage());
            e.printStackTrace();
        }

        return brandConfigs;
    }
}

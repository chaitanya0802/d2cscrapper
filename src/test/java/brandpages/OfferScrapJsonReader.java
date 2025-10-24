package brandpages;

import java.io.FileReader;
import java.util.*;
import com.google.gson.*;

public class OfferScrapJsonReader {

    public static Map<String, OfferScrapConfig> loadBrandConfigs(String jsonPath) {
        Map<String, OfferScrapConfig> brandConfigs = new HashMap<>();

        try (FileReader reader = new FileReader(jsonPath)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject brands = root.getAsJsonObject("brands");

            for (Map.Entry<String, JsonElement> brandEntry : brands.entrySet()) {
                String brandName = brandEntry.getKey().toLowerCase();
                JsonObject brandObj = brandEntry.getValue().getAsJsonObject();

                // Locators
                Map<String, String> locators = new HashMap<>();
                JsonObject locObj = brandObj.getAsJsonObject("locators");
                if (locObj != null) {
                    for (Map.Entry<String, JsonElement> e : locObj.entrySet()) {
                        locators.put(e.getKey(), e.getValue().getAsString());
                    }
                }

                // Main category
                String mainCategory = "";
                if (brandObj.has("maincategory") && !brandObj.get("maincategory").isJsonNull()) {
                    mainCategory = brandObj.get("maincategory").getAsString();
                }

                // offer_page_url (array)
                List<String> offerPageUrls = new ArrayList<>();
                if (brandObj.has("offer_page_url") && brandObj.get("offer_page_url").isJsonArray()) {
                    JsonArray arr = brandObj.get("offer_page_url").getAsJsonArray();
                    for (JsonElement el : arr) {
                        if (!el.isJsonNull()) {
                            offerPageUrls.add(el.getAsString());
                        }
                    }
                }

                // Store related
                int store_id = brandObj.has("store_id") && !brandObj.get("store_id").isJsonNull()
                        ? brandObj.get("store_id").getAsInt()
                        : 0;

                String store_name = brandObj.has("store_name") && !brandObj.get("store_name").isJsonNull()
                        ? brandObj.get("store_name").getAsString()
                        : "";

                String store_url = brandObj.has("store_url") && !brandObj.get("store_url").isJsonNull()
                        ? brandObj.get("store_url").getAsString()
                        : "";

                // Store in map
                OfferScrapConfig config = new OfferScrapConfig(
                        locators, mainCategory, offerPageUrls, store_id, store_name, store_url
                );
                brandConfigs.put(brandName, config);
            }

            System.out.println("Loaded " + brandConfigs.size() + " offer brand configurations from JSON.");

        } catch (Exception e) {
            System.out.println("!!! Error reading JSON: " + e.getMessage());
            e.printStackTrace();
        }

        return brandConfigs;
    }
}


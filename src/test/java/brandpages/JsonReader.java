package brandpages;

import java.io.FileReader;
import java.util.*;
import com.google.gson.*;

/**
 * Reads and parses brands.json into a Map<String, BrandConfig>
 */
public class JsonReader {

    public static Map<String, BrandConfig> loadBrandConfigs(String jsonPath) {
        Map<String, BrandConfig> brandConfigs = new HashMap<>();

        try (FileReader reader = new FileReader(jsonPath)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject brands = root.getAsJsonObject("brands");

            for (Map.Entry<String, JsonElement> brandEntry : brands.entrySet()) {
                String brandName = brandEntry.getKey().toLowerCase();
                JsonObject brandObj = brandEntry.getValue().getAsJsonObject();

                // ✅ Locators
                Map<String, String> locators = new HashMap<>();
                JsonObject locObj = brandObj.getAsJsonObject("locators");
                if (locObj != null) {
                    for (Map.Entry<String, JsonElement> e : locObj.entrySet()) {
                        locators.put(e.getKey(), e.getValue().getAsString());
                    }
                }

                // ✅ Main category
                String mainCategory = "";
                if (brandObj.has("maincategory")) {
                    mainCategory = brandObj.get("maincategory").getAsString();
                }

                // ✅ Subcategories
                Map<String, String> subcategories = new HashMap<>();
                if (brandObj.has("subcategories")) {
                    JsonObject subObj = brandObj.getAsJsonObject("subcategories");
                    for (Map.Entry<String, JsonElement> e : subObj.entrySet()) {
                        subcategories.put(e.getKey(), e.getValue().getAsString());
                    }
                }

                // ✅ Store in map
                BrandConfig config = new BrandConfig(locators, mainCategory, subcategories);
                brandConfigs.put(brandName, config);
            }

            System.out.println("✅ Loaded " + brandConfigs.size() + " brand configurations from JSON.");

        } catch (Exception e) {
            System.out.println("❌ Error reading JSON: " + e.getMessage());
            e.printStackTrace();
        }

        return brandConfigs;
    }
}

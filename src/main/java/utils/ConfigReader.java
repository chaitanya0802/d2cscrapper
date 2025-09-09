package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    private static Properties properties;

    // Load config.properties when the class is loaded
    static {
        try {
            FileInputStream inputStream = new FileInputStream("src/test/resources/config/config.properties");
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load config.properties file.");
        }
    }

    // Get the property value for a given key
    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Property '" + key + "' not found in config.properties file.");
        }
        return value;
    }

    // Get int value
    public static int getIntProperty(String key) {
        return Integer.parseInt(getProperty(key));
    }

    // Get boolean value
    public static boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }
}

package utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProductPoster {

    private static final String BACKEND_URL = "https://your-backend.com/api/products/bulk";
    private static final String LOGIN_URL   = "https://your-backend.com/api/login";
    private static final String PASSWORD    = "hEuH@Rf54%$fs";

    // Get access token from /login
    public static String getAccessToken(String storename) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Prepare login JSON
        String loginBody = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", storename+"@email.com", PASSWORD);

        URL url = new URL(LOGIN_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(loginBody.getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Failed to login. HTTP Code: " + responseCode);
        }

        // Read response
        StringBuilder response;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            response = new StringBuilder();
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        conn.disconnect();

        // Parse JSON to get token
        JsonNode json = mapper.readTree(response.toString());
        String token = json.get("token").asText(); // change "token" if backend uses different key

        return token;
    }

    // Post JSON array of products
    public static boolean postProducts(List<Product> products, String storename) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(products);

        URL url = new URL(BACKEND_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        // Set access token dynamically
        conn.setRequestProperty("Authorization", "Bearer " + getAccessToken(storename));

        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        boolean success = (responseCode == 200 || responseCode == 201);
        conn.disconnect();
        return success;

    }
}

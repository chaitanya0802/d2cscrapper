package utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONArray;
import org.json.JSONObject;

public class OCRTextExtraction{

    static String apiEndpoint = "https://api.ocr.space/parse/image";
    static String apiKey = "K88701891288957";
    static String language = "eng";
    static boolean isOverlayRequired = false;
    static boolean detectOrientation = true;
    static boolean scale = true;
    static int ocrEngine = 2;

    public static String getImageText(String imageUrl) {
        try {

            URL url = new URL(apiEndpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("apikey", apiKey);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            StringBuilder params = new StringBuilder();
            params.append("url=").append(URLEncoder.encode(imageUrl, "UTF-8"));
            params.append("&language=").append(language);
            params.append("&isOverlayRequired=").append(isOverlayRequired);
            params.append("&detectOrientation=").append(detectOrientation);
            params.append("&scale=").append(scale);
            params.append("&OCREngine=").append(ocrEngine);

            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.writeBytes(params.toString());
                wr.flush();
            }

            //read res
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            //parse
            JSONObject json = new JSONObject(response.toString());

            if (json.getBoolean("IsErroredOnProcessing")) {
                return "";
            }

            JSONArray results = json.getJSONArray("ParsedResults");
            StringBuilder outputText = new StringBuilder();

            for (int i = 0; i < results.length(); i++) {
                JSONObject res = results.getJSONObject(i);
                String text = res.optString("ParsedText", "");
                if (!text.isEmpty()) {
                    outputText.append(text.trim()).append("\n");
                }
            }

            return outputText.toString().trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}

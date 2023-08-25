package fr.phoenix.aibuilds.communication;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.phoenix.aibuilds.AIBuilds;
import fr.phoenix.aibuilds.communication.responsehandler.ShapEPromptResponseHandler;
import fr.phoenix.aibuilds.communication.responsehandler.TwoDResponseHandler;
import fr.phoenix.aibuilds.utils.message.Message;
import org.apache.commons.lang.Validate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CommunicationHandler {
    private final ConstructionHandler constructionHandler;

    public CommunicationHandler(ConstructionHandler constructionHandler) {
        this.constructionHandler = constructionHandler;
    }

    public void request(String[] input, RequestType requestType) throws IOException {

        URL url = new URL("https://api.replicate.com/v1/predictions");
        String data = requestType.processInput(input);
        URL getURL = requestURL(url, data);
        requestType.handleResponse(constructionHandler, getURL);
    }

    public URL requestURL(URL url, String data) throws IOException {
        String apiToken = AIBuilds.plugin.configManager.APIToken;
        byte[] postDataBytes = data.toString().getBytes("UTF-8");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Token " + apiToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        //Error management.
        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            // read the error message from the BufferedReader
            String inputLine;
            StringBuffer errorMessage = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                errorMessage.append(inputLine);
            }
            in.close();
            Message.ERROR_OCCURRED_WITH_API_CALL.format("error-code", responseCode, "error-message", errorMessage.toString()).send(constructionHandler.getPlayer());
            return null;
        }

        //Reads the response
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        Validate.isTrue(response.toString() != "", "No response received from replicate API");
        JsonElement jsonElement = JsonParser.parseString(response.toString());
        Validate.isTrue(jsonElement.isJsonObject() && !jsonElement.isJsonNull(),
                "A problem occurred when connecting to replicate API," +
                        " check your replicate logs to see if the API call was received:" + response);

        JsonObject object = JsonParser.parseString(response.toString()).getAsJsonObject();
        Validate.isTrue(object.has("urls"), "No webhook urls found in the response: " + response);
        Validate.isTrue(object.get("urls").getAsJsonObject().has("get"), "No get url found in the response:" + response);
        URL getUrl = new URL(object.get("urls").getAsJsonObject().get("get").getAsString());
        Validate.notNull(getUrl, "No get url found in the response:" + response);
        return getUrl;
    }

}

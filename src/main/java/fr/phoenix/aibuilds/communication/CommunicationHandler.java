package fr.phoenix.aibuilds.communication;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.phoenix.aibuilds.AIBuilds;
import fr.phoenix.aibuilds.communication.responsehandler.ResponseHandler;
import fr.phoenix.aibuilds.communication.responsehandler.ThreeDResponseHandler;
import fr.phoenix.aibuilds.communication.responsehandler.TwoDResponseHandler;
import fr.phoenix.aibuilds.utils.message.Message;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CommunicationHandler {

    private final String SHAPE_VERSION="eb4d94fa0ba851ba949775e1d6b1966181981799538e01fb6f43883476cc5a66";
    private final ConstructionHandler constructionHandler;
    private final int GRID_SIZE = 10;

    public CommunicationHandler(ConstructionHandler constructionHandler) {
        this.constructionHandler = constructionHandler;
    }


    public void request2D(URL url, String data) throws IOException {
        URL getURL = requestURL(url, data);
        new TwoDResponseHandler(constructionHandler, getURL).runTaskTimer(AIBuilds.plugin, 0, AIBuilds.plugin.configManager.progressBarUpdateTime);
    }

    public void request3D(URL url, String data) throws IOException {
        URL getURL = requestURL(url, data);
        new ThreeDResponseHandler(constructionHandler, getURL).runTaskTimer(AIBuilds.plugin, 0, AIBuilds.plugin.configManager.progressBarUpdateTime);
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
        return getUrl;
    }

    public void requestPrompt(String prompt) throws IOException {
        URL url = new URL("https://api.replicate.com/v1/predictions");
        String data = "{\"version\":\"" + SHAPE_VERSION+ "\"" +
                ",\"input\":{\"prompt\":\"" + prompt + "\",\"grid_size\":" + GRID_SIZE + "}}";
        request3D(url, data);
    }

    public void requestImage(String imageUrl) throws IOException {
        URL url = new URL("https://api.replicate.com/v1/predictions");
        String data = "{\"version\":\"" + SHAPE_VERSION+ "\"" +
                ",\"input\":{\"image\":\"" + imageUrl + "\",\"grid_size\":" + GRID_SIZE + "}}";
        request3D(url, data);

    }

    public void request2DImage(String prompt) throws IOException {
        URL url = new URL("https://api.replicate.com/v1/predictions");
        String data = "{\"version\":\"" + "db21e45d3f7023abc2a46ee38a23973f6dce16bb082a930b0c49861f96d1e5bf" + "\"" +
                ",\"input\":{\"prompt\":\"" + prompt + "\",\"image_dimensions\":\"512x512\"}}";
        request2D(url, data);

    }
}

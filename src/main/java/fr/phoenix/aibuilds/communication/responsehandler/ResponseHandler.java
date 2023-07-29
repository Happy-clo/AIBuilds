package fr.phoenix.aibuilds.communication.responsehandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.phoenix.aibuilds.AIBuilds;
import fr.phoenix.aibuilds.communication.ConstructionHandler;
import fr.phoenix.aibuilds.placeholders.Placeholders;
import fr.phoenix.aibuilds.utils.ActionBarRunnable;
import fr.phoenix.aibuilds.utils.Utils;
import fr.phoenix.aibuilds.utils.message.Message;
import org.apache.commons.lang.Validate;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class ResponseHandler extends BukkitRunnable {
    protected final ConstructionHandler constructionHandler;
    protected final URL url;
    protected final ActionBarRunnable actionBarRunnable;
    protected int progress;
    protected final Placeholders holders;
    protected boolean processingStarted = false;
    protected final long creationTime;

    public ResponseHandler(ConstructionHandler constructionHandler, URL url) {
        this.constructionHandler = constructionHandler;
        this.url = url;
        this.progress = 0;
        holders = new Placeholders();
        holders.register("progress-bar", getProgressBar());
        holders.register("progress", progress);
        holders.register("waited-time", Utils.formatTime(0));
        actionBarRunnable = new ActionBarRunnable(constructionHandler.getPlayer(), Message.SERVER_COLD_BOOT.format().getAsString(), holders);
        creationTime = System.currentTimeMillis();
    }

    public String getProgressBar() {
        String progressBar = "";
        int filled = progress / 4;
        for (int i = 0; i < filled; i++) {
            progressBar += "\u25A0"; // full square character
        }
        for (int i = 0; i < 25 - filled; i++) {
            progressBar += "\u25A1"; // empty square character
        }
        return progressBar;
    }

    @Override
    public void run() {
        try {
            String apiToken = AIBuilds.plugin.configManager.APIToken;
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Token " + apiToken);
            conn.setRequestProperty("Content-Type", "application/json");

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
                return;
            }
            //Parse input stream.
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            JsonObject object = JsonParser.parseString(response.toString()).getAsJsonObject();
            Validate.isTrue(object.has("status"),
                    "The response from the API does not contain an status field:" + response);
            String status = object.get("status").getAsString();
            //If the calculation is still ongoing.
            if (status.equalsIgnoreCase("starting") || status.equalsIgnoreCase("processing")) {
                showProgression(object);
            }
            //If there is the result as an output
            else {
                if (status.equalsIgnoreCase("succeeded"))
                    parseJson(object);
                else if (status.equalsIgnoreCase("failed"))
                    Message.PREDICTION_FAILED.format().send(constructionHandler.getPlayer());
                else Message.PREDICTION_CANCELED.format().send(constructionHandler.getPlayer());
                this.cancel();
                actionBarRunnable.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
            actionBarRunnable.cancel();
            this.cancel();
        }
    }

    public abstract void showProgression(JsonObject object);

    public abstract void parseJson(JsonObject object);
}

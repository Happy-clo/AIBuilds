package fr.phoenix.aibuilds.communication.responsehandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.phoenix.aibuilds.AIBuilds;
import fr.phoenix.aibuilds.communication.ConstructionHandler;
import fr.phoenix.aibuilds.utils.Utils;
import fr.phoenix.aibuilds.utils.message.Message;

import java.net.URL;

public class PointEImageResponseHandler extends ResponseHandler {
    public PointEImageResponseHandler(ConstructionHandler constructionHandler, URL url) {
        super(constructionHandler, url);
    }

    @Override
    public void showProgression(JsonObject object) {

        if (object.get("logs").isJsonNull()) {
            holders.register("waited-time", Utils.formatTime(System.currentTimeMillis() - creationTime));
        } else {

            String[] logs = object.get("logs").getAsString().split("\n");
            if (logs.length <= 1)
                holders.register("waited-time", Utils.formatTime(System.currentTimeMillis() - creationTime));
            else {
                if (!processingStarted) {
                    processingStarted = true;
                    actionBarRunnable.setMsg(Message.PROGRESS_MESSAGE.format().getAsString());
                }
                String progressString = logs[logs.length - 1];
                int iteration;
                try {
                    iteration = Integer.parseInt(progressString.substring(0, progressString.indexOf("i")));
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    AIBuilds.log("Error Message: " + progressString);
                    cancel();
                    actionBarRunnable.cancel();
                    return;
                }
                progress = (int) ((iteration / 130.) * 100);
                //If there is a log we update the progression
                //We update the placeholder.
                holders.register("progress", progress);
                holders.register("progress-bar", getProgressBar());
            }
        }
    }

    @Override
    public void parseJson(JsonObject object) {

        JsonObject jsonFile = object.get("output").getAsJsonObject();
        jsonFile = jsonFile.get("json_file").getAsJsonObject();
        JsonArray colors = jsonFile.get("colors").getAsJsonArray();
        JsonArray coords = jsonFile.get("coords").getAsJsonArray();
        int n = colors.size();
        double[][][] points = new double[1][n][6];
        for (int i = 0; i < n; i++) {
            JsonArray color = colors.get(i).getAsJsonArray();
            JsonArray coord = coords.get(i).getAsJsonArray();
            points[0][i][0] = coord.get(0).getAsDouble();
            points[0][i][1] = coord.get(1).getAsDouble();
            points[0][i][2] = coord.get(2).getAsDouble();
            points[0][i][3] = color.get(0).getAsDouble() * 255;
            points[0][i][4] = color.get(1).getAsDouble() * 255;
            points[0][i][5] = color.get(2).getAsDouble() * 255;
        }
        constructionHandler.handleResponse(points, null);
    }
}


package fr.phoenix.aibuilds.communication.responsehandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.phoenix.aibuilds.AIBuilds;
import fr.phoenix.aibuilds.communication.ConstructionHandler;
import fr.phoenix.aibuilds.utils.Utils;
import fr.phoenix.aibuilds.utils.message.Message;
import org.apache.commons.lang.Validate;

import java.net.URL;

public class ThreeDResponseHandler extends ResponseHandler {
    public ThreeDResponseHandler(ConstructionHandler constructionHandler, URL url) {
        super(constructionHandler, url);
        actionBarRunnable.runTaskTimer(AIBuilds.plugin, 0, 10L);
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
                progressString=progressString.replace(" ","");
                try {
                    progress = Integer.parseInt(progressString.substring(0, progressString.indexOf("%")));
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    AIBuilds.log("Error while parsing the progression: " + progressString);
                    AIBuilds.log("Tried to parse: " + progressString.substring(0, progressString.indexOf("%")) + " as an integer.");
                    cancel();
                    actionBarRunnable.cancel();
                    return;
                }
                //If there is a log we update the progression
                //We update the placeholder.11
                holders.register("progress", progress);
                holders.register("progress-bar", getProgressBar());
            }
        }

    }

    @Override
    public void parseJson(JsonObject object) {
        Validate.isTrue(object.has("output"), "The response does not contain an output: " + object);
        JsonArray output = object.get("output").getAsJsonArray();
        JsonObject jsonFile = output.get(0).getAsJsonObject();
        Validate.isTrue(jsonFile.has("points"), "The output does not contain a points array: " + jsonFile);
        JsonArray points = jsonFile.get("points").getAsJsonArray();
        int n = points.size();
        double[][] result = new double[n][7];
        for (int i = 0; i < n; i++) {
            JsonArray point = points.get(i).getAsJsonArray();
            result[i][0] = point.get(0).getAsDouble();
            result[i][1] = point.get(1).getAsDouble();
            result[i][2] = point.get(2).getAsDouble();
            result[i][3] = point.get(3).getAsDouble() * 255;
            result[i][4] = point.get(4).getAsDouble() * 255;
            result[i][5] = point.get(5).getAsDouble() * 255;
            result[i][6] = point.get(6).getAsDouble();
        }
        constructionHandler.handleResponse(result);
        actionBarRunnable.cancel();
        cancel();
    }
}

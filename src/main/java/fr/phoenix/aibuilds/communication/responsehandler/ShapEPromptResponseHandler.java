package fr.phoenix.aibuilds.communication.responsehandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.phoenix.aibuilds.AIBuilds;
import fr.phoenix.aibuilds.communication.ConstructionHandler;
import fr.phoenix.aibuilds.utils.Utils;
import fr.phoenix.aibuilds.utils.message.Message;
import org.apache.commons.lang.Validate;

import java.net.URL;

public class ShapEPromptResponseHandler extends ResponseHandler {
    public ShapEPromptResponseHandler(ConstructionHandler constructionHandler, URL url) {
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
                progressString = progressString.replace(" ", "");
                try {
                    progress = Integer.parseInt(progressString.substring(0, progressString.indexOf("%")));
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    if (AIBuilds.plugin.configManager.debug) {
                        AIBuilds.log("Error while parsing the progression: " + progressString);
                        AIBuilds.log("Tried to parse: " + progressString.substring(0, progressString.indexOf("%")) + " as an integer.");
                    }
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
        JsonObject output = object.get("output").getAsJsonObject();
        Validate.isTrue(output.has("result"), "The output does not contain a result: " + output);
        JsonArray jsonArray = output.get("result").getAsJsonArray();
        int batches = jsonArray.size();
        double[][][] allPoints = new double[batches][][];
        int[][][] allFaces = new int[batches][][];
        for (int i = 0; i < batches; i++) {
            JsonObject jsonFile = jsonArray.get(i).getAsJsonObject();
            Validate.isTrue(jsonFile.has("vertices"), "The output does not contain a vertices array: " + jsonFile);
            JsonArray pointsArray = jsonFile.get("vertices").getAsJsonArray();
            int n = pointsArray.size();
            double[][] points = new double[n][6];
            for (int j = 0; j < n; j++) {
                JsonArray point = pointsArray.get(j).getAsJsonArray();
                points[j][0] = point.get(0).getAsDouble();
                points[j][1] = point.get(1).getAsDouble();
                points[j][2] = point.get(2).getAsDouble();
                points[j][3] = point.get(3).getAsDouble() * 255;
                points[j][4] = point.get(4).getAsDouble() * 255;
                points[j][5] = point.get(5).getAsDouble() * 255;
            }
            Validate.isTrue(jsonFile.has("faces"), "The output does not contain a faces array: " + jsonFile);
            JsonArray facesArray = jsonFile.get("faces").getAsJsonArray();
            int m = facesArray.size();
            int[][] faces = new int[m][3];
            for (int j = 0; j < m; j++) {
                JsonArray face = facesArray.get(j).getAsJsonArray();
                faces[j][0] = face.get(0).getAsInt();
                faces[j][1] = face.get(1).getAsInt();
                faces[j][2] = face.get(2).getAsInt();
            }
            allPoints[i] = points;
            allFaces[i] = faces;
        }

        constructionHandler.handleResponse(allPoints, allFaces);
    }
}

package fr.phoenix.aibuilds.communication.responsehandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.phoenix.aibuilds.communication.ConstructionHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class TwoDResponseHandler extends ResponseHandler {

    public TwoDResponseHandler(ConstructionHandler constructionHandler, URL url) {
        super(constructionHandler, url);

    }

    @Override
    public void showProgression(JsonObject object) {

    }

    @Override
    public void parseJson(JsonObject object) {
        try {

            URL url = new URL(object.get("output").getAsString());
            BufferedImage image = ImageIO.read(url);
            int width = image.getWidth();
            int height = image.getHeight();
            double[][][] points = new double[1][width * height][6];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pos = x + y * width;
                    int rgb = image.getRGB(x, y);
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    points[0][pos][0] = -((double) x) / width + 0.5;
                    points[0][pos][1] = 0;
                    points[0][pos][2] = -((double) y) / height + 0.5;
                    ;
                    points[0][pos][3] = red;
                    points[0][pos][4] = green;
                    points[0][pos][5] = blue;
                }
            }
            constructionHandler.handleResponse(points, null);
        } catch (IOException e) {
            e.printStackTrace();
            cancel();
        }

    }

}

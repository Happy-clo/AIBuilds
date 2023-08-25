package fr.phoenix.aibuilds.communication;

import fr.phoenix.aibuilds.AIBuilds;
import fr.phoenix.aibuilds.communication.responsehandler.PointEImageResponseHandler;
import fr.phoenix.aibuilds.communication.responsehandler.ResponseHandler;
import fr.phoenix.aibuilds.communication.responsehandler.ShapEPromptResponseHandler;
import fr.phoenix.aibuilds.communication.responsehandler.TwoDResponseHandler;

import java.net.URL;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public enum RequestType {

    POINTE_IMAGE(
            (input) -> "{\"version\":\"e10281e914000d15c6274c49dfed579aab91eb8d8003fb1df098481e903fa1c7\"" +
                    ",\"input\":{\"image\":\"" + input[0] + "\"}}",
            (constructionHandler, getURL) -> new PointEImageResponseHandler(constructionHandler, getURL)),
    SHAPE_PROMPT(
            (input) -> "{\"version\":\"ac9d5d031b897e0eb63a0dbc068c9ac8e7928b72b666f663b204709f1b12dd30\"" +
                    ",\"input\":{\"prompt\":\"" + input[0] + "\",\"batch_size\":" + input[1] + "}}"
            , (constructionHandler, getURL) -> new ShapEPromptResponseHandler(constructionHandler, getURL)),
    TWOD((input) -> "{\"version\":\"db21e45d3f7023abc2a46ee38a23973f6dce16bb082a930b0c49861f96d1e5bf\"" +
            ",\"input\":{\"prompt\":\"" + input[0] + "\",\"image_dimensions\":\"512x512\"}}"
            , (constructionHandler, getURL) -> new TwoDResponseHandler(constructionHandler, getURL));

    private final BiFunction<ConstructionHandler, URL, ResponseHandler> responseHandlerSupplier;

    private final Function<String[], String> processInput;

    RequestType(Function<String[], String> processInput, BiFunction<ConstructionHandler, URL, ResponseHandler> responseHandlerSupplier) {
        this.responseHandlerSupplier = responseHandlerSupplier;
        this.processInput = processInput;
    }

    public void handleResponse(ConstructionHandler constructionHandler, URL getURL) {
        responseHandlerSupplier.apply(constructionHandler, getURL).runTaskTimer(AIBuilds.plugin, 0, AIBuilds.plugin.configManager.progressBarUpdateTime);
    }

    public String processInput(String[] input) {
        return processInput.apply(input);
    }

}

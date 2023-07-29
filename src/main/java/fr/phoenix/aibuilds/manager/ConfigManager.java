package fr.phoenix.aibuilds.manager;

import fr.phoenix.aibuilds.AIBuilds;
import fr.phoenix.aibuilds.utils.ConfigFile;
import fr.phoenix.aibuilds.utils.message.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ConfigManager {
    public int anglePerRotation, blocksPerTranslation, blocksPerUpsize, defaultSize, promptToken, lowToken, midToken, highToken, twoDToken;
    public long progressBarUpdateTime;
    public double thresholdIncrease;
    public String APIToken, modelVersion;

    public void load(boolean clearBefore) {
        thresholdIncrease = AIBuilds.plugin.getConfig().getDouble("threshold-increase");
        anglePerRotation = AIBuilds.plugin.getConfig().getInt("angle-per-rotation");
        blocksPerTranslation = AIBuilds.plugin.getConfig().getInt("blocks-per-translation");
        blocksPerUpsize = AIBuilds.plugin.getConfig().getInt("blocks-per-upsize");
        APIToken = AIBuilds.plugin.getConfig().getString("api-token");
        modelVersion = AIBuilds.plugin.getConfig().getString("model-version");
        progressBarUpdateTime = AIBuilds.plugin.getConfig().getLong("progress-bar-update-time");
        defaultSize = AIBuilds.plugin.getConfig().getInt("default-size");
        promptToken = AIBuilds.plugin.getConfig().getInt("token-consumption.prompt");
        lowToken = AIBuilds.plugin.getConfig().getInt("token-consumption.low");
        midToken = AIBuilds.plugin.getConfig().getInt("token-consumption.mid");
        highToken = AIBuilds.plugin.getConfig().getInt("token-consumption.high");
        twoDToken = AIBuilds.plugin.getConfig().getInt("token-consumption.2d");
        loadDefaultFile("palette.yml");

        // Save default messages
        ConfigFile messages = new ConfigFile("/language", "messages");
        for (Message key : Message.values()) {
            String path = key.getPath();
            if (!messages.getConfig().contains(path)) {
                messages.getConfig().set(path + ".format", key.getCached());
                if (key.hasSound()) {
                    messages.getConfig().set(path + ".sound.name", key.getSound().getSound().name());
                    messages.getConfig().set(path + ".sound.vol", key.getSound().getVolume());
                    messages.getConfig().set(path + ".sound.pitch", key.getSound().getPitch());
                }
            }
        }
        messages.save();
    }


    public void loadDefaultFile(String name) {
        loadDefaultFile("", name);
    }

    public void loadDefaultFile(String path, String name) {
        String newPath = path.isEmpty() ? "" : "/" + path;
        File folder = new File(AIBuilds.plugin.getDataFolder() + (newPath));
        if (!folder.exists()) folder.mkdir();

        File file = new File(AIBuilds.plugin.getDataFolder() + (newPath), name);
        if (!file.exists()) try {
            Files.copy(AIBuilds.plugin.getResource("default/" + (path.isEmpty() ? "" : path + "/") + name), file.getAbsoluteFile().toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

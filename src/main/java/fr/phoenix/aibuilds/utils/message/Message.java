package fr.phoenix.aibuilds.utils.message;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Message {
    SERVER_COLD_BOOT("&7Cold Boot of the server &f- " +
            "&7Time waited: &6{waited-time} &f- " +
            "&7Estimated waiting time: &65min"),

    PROGRESS_MESSAGE("&f{progress-bar} &7{progress}%."),

    NOT_ENOUGH_TOKEN("&7You can't create a build, you need &6{needed-tokens} &7but only have &6{owned-tokens}&7 tokens."),

    PLAYER_TOKENS("&6{player} &7now has &6{token} &7tokens."),

    CURRENT_TOKEN("&7You used &6{consumed-token}&7 tokens and now have &6{token}&7."),

    ERROR_OCCURRED_WITH_API_CALL("&7An error occurred while calling the API: HTTP Error code error-code}"
            , " Error Message: {error-message} "),

    PREDICTION_FAILED("&7The prediction failed, check the logs on replicate to see what went wrong."),
    PREDICTION_CANCELED("&7The prediction was canceled."),
    NO_API_TOKEN("&7You didn't fill any API key in the config.yml, this is required for this plugin to work properly."),

    CONSTRUCTION_MODIFICATION("&a{current-batch}/{max-batch}&7|&6slot&aPush&7-&6slot&aPull&7-&6slot&aCW&7-&6slot&aAnti CW" +
            "&7-&6slot&aUpsize&7-&6slot&aDownsize&7-&6slot&aConfirm&7-&6slot&aDelete"),

    CONSTRUCTION_BEING_GENERATED("&7You can't generate a new construction as one is already being generated."),

    CONSTRUCTION_ACCEPTED("&7The construction was successfully accepted, you can't modify it now."),

    CONSTRUCTION_REMOVED("&7The construction was successfully removed.");

    private List<String> message;
    private SoundReader sound;

    private Message(String... message) {
        this(null, message);
    }

    private Message(SoundReader sound, String... message) {
        this.message = Arrays.asList(message);
        this.sound = sound;
    }

    public String getPath() {
        return name().toLowerCase().replace("_", "-");
    }

    /**
     * Deep Copy !!
     *
     * @return Message updated based on what's in the config files
     */
    public List<String> getCached() {
        return new ArrayList<>(message);
    }

    public SoundReader getSound() {
        return sound;
    }

    public boolean hasSound() {
        return sound != null;
    }

    public PlayerMessage format(Object... placeholders) {
        return new PlayerMessage(this).format(placeholders);
    }

    public void update(ConfigurationSection config) {
        List<String> format = config.getStringList("format");
        Validate.notNull(this.message = format, "Could not read message format");
        sound = config.contains("sound") ? new SoundReader(config.getConfigurationSection("sound")) : null;
    }
}
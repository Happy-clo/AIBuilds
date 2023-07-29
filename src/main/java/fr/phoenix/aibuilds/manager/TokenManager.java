package fr.phoenix.aibuilds.manager;

import fr.phoenix.aibuilds.utils.ConfigFile;
import me.clip.placeholderapi.libs.kyori.adventure.util.HSVLike;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TokenManager {
    private final Map<UUID, Integer> playerTokens = new HashMap<>();


    public int getToken(Player player) {
        return playerTokens.get(player.getUniqueId());
    }

    public void setToken(Player player, int token) {
        playerTokens.put(player.getUniqueId(), token);
    }


    public void giveToken(Player player, int give) {
        playerTokens.put(player.getUniqueId(), playerTokens.get(player.getUniqueId()) + give);
    }

    public void load(Player player) {
        ConfigFile config = new ConfigFile("playerdata");
        playerTokens.put(player.getUniqueId(), config.getConfig().getInt(player.getUniqueId().toString(), 0));
    }

    public void save(Player player) {
        ConfigFile config = new ConfigFile("playerdata");
        config.getConfig().set(player.getUniqueId().toString(), playerTokens.get(player.getUniqueId()));
        playerTokens.remove(player.getUniqueId());
        config.save();
    }


}

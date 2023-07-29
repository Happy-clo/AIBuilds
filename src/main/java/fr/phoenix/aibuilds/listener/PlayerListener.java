package fr.phoenix.aibuilds.listener;

import fr.phoenix.aibuilds.AIBuilds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        AIBuilds.plugin.tokenManager.load(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        AIBuilds.plugin.tokenManager.save(e.getPlayer());
    }
}

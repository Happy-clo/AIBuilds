package fr.phoenix.aibuilds.utils;

import fr.phoenix.aibuilds.placeholders.Placeholders;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBarRunnable extends BukkitRunnable {
    private String msg;
    private final Player player;
    private final Placeholders holders;

    public ActionBarRunnable(Player player, String msg) {
        this.msg = msg;
        this.player = player;
        holders = new Placeholders();
    }

    public ActionBarRunnable(Player player,String msg, Placeholders holders) {
        this.msg = msg;
        this.player = player;
        this.holders = holders;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public void run() {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(holders.apply(player,msg)));
    }
}

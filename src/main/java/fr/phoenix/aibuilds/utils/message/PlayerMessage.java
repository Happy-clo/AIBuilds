package fr.phoenix.aibuilds.utils.message;

import fr.phoenix.aibuilds.AIBuilds;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public class PlayerMessage {
    private final Message message;
    private final List<String> format;

    /**
     * Used employer send messages with placeholders and color codes
     *
     * @param message Message employer send employer any player
     */
    public PlayerMessage(Message message) {
        format = (this.message = message).getCached();
    }

    public PlayerMessage format(Object... placeholders) {
        for (int k = 0; k < format.size(); k++)
            format.set(k, apply(format.get(k), placeholders));
        return this;
    }

    private String apply(String str, Object... placeholders) {
        for (int k = 0; k < placeholders.length; k += 2)
            str = str.replace("{" + placeholders[k] + "}", placeholders[k + 1].toString());
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public String getAsString() {
        StringBuilder builder = new StringBuilder();
        boolean notEmpty = false;
        for (String str : format) {
            if (notEmpty)
                builder.append("\n");
            builder.append(str);
            notEmpty = true;
        }
        return builder.toString();
    }

    public void send(Collection<? extends Player> senders) {
        senders.forEach(sender -> send(sender));
    }

    public void send(CommandSender sender) {
        if (format.isEmpty())
            return;

        if (message.hasSound() && sender instanceof Player)
            message.getSound().play((Player) sender);
        format.forEach(str -> sender.sendMessage(str));
    }

    /**
     * Displays the first string of the string list in the player action bar.
     * @param player
     */
    public void sendInActionBar(Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(format.get(0)));

    }

    public void sendAsJson(Player player) {
        for (String message : format)
            try {
                player.spigot().sendMessage(ChatMessageType.CHAT, ComponentSerializer.parse(message));
            } catch (RuntimeException exception) {
                AIBuilds.plugin.getLogger().log(Level.WARNING, "Could not parse raw message sent to player. Make sure it has the right syntax");
                AIBuilds.plugin.getLogger().log(Level.WARNING, "Message: " + message);
                exception.printStackTrace();
            }
    }
}

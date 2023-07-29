package fr.phoenix.aibuilds.command;

import fr.phoenix.aibuilds.AIBuilds;
import fr.phoenix.aibuilds.command.objects.CommandTreeNode;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ReloadTreeNode extends CommandTreeNode {
    public ReloadTreeNode(CommandTreeNode parent) {
        super(parent, "reload");
    }

    @Override
    public CommandTreeNode.CommandResult execute(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.YELLOW + "Reloading " + AIBuilds.plugin.getName() + " " + AIBuilds.plugin.getDescription().getVersion() + "...");
        long ms = System.currentTimeMillis();

        AIBuilds.plugin.initializePlugin(true);

        ms = System.currentTimeMillis() - ms;
        sender.sendMessage(ChatColor.YELLOW + AIBuilds.plugin.getName() + " " + AIBuilds.plugin.getDescription().getVersion() + " successfully reloaded.");
        sender.sendMessage(ChatColor.YELLOW + "Time Taken: " + ChatColor.GOLD + ms + ChatColor.YELLOW + "ms (" + ChatColor.GOLD + (double) ms / 50 + ChatColor.YELLOW + " ticks)");
        return CommandResult.SUCCESS;


    }
}

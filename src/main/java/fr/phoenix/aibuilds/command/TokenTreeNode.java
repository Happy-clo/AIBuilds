package fr.phoenix.aibuilds.command;

import fr.phoenix.aibuilds.AIBuilds;
import fr.phoenix.aibuilds.command.objects.CommandTreeNode;
import fr.phoenix.aibuilds.command.objects.parameter.Parameter;
import fr.phoenix.aibuilds.utils.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class TokenTreeNode extends CommandTreeNode {
    private final Function<Player, Integer> get;

    public TokenTreeNode(CommandTreeNode parent) {
        super(parent, "tokens");
        this.get = (player) -> AIBuilds.plugin.tokenManager.getToken(player);

        addChild(new ActionCommandTreeNode(this, "set", (player, token) -> AIBuilds.plugin.tokenManager.setToken(player, token)));
        addChild(new ActionCommandTreeNode(this, "give", (player, token) -> AIBuilds.plugin.tokenManager.giveToken(player, token)));
    }

    public class ActionCommandTreeNode extends CommandTreeNode {
        private final BiConsumer<Player, Integer> action;

        public ActionCommandTreeNode(CommandTreeNode parent, String type, BiConsumer<Player, Integer> action) {
            super(parent, type);

            this.action = action;

            addParameter(Parameter.PLAYER);
            addParameter(Parameter.AMOUNT);
        }

        @Override
        public CommandResult execute(CommandSender sender, String[] args) {
            if (args.length < 4)
                return CommandResult.THROW_USAGE;
            if(!sender.hasPermission("aibuilds.admin")) {

            }

            Player player = Bukkit.getPlayer(args[2]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[2] + ".");
                return CommandResult.FAILURE;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[3]);
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + args[3] + " is not a valid number.");
                return CommandResult.FAILURE;
            }

            action.accept(player, amount);
            Message.PLAYER_TOKENS.format("player", player.getName(), "token", AIBuilds.plugin.tokenManager.getToken(player)).send(sender);
            return CommandResult.SUCCESS;
        }
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }
}



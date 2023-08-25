package fr.phoenix.aibuilds.command;

import fr.phoenix.aibuilds.AIBuilds;
import fr.phoenix.aibuilds.command.objects.CommandTreeNode;
import fr.phoenix.aibuilds.command.objects.parameter.Parameter;
import fr.phoenix.aibuilds.communication.CommunicationHandler;
import fr.phoenix.aibuilds.communication.ConstructionHandler;
import fr.phoenix.aibuilds.communication.RequestType;
import fr.phoenix.aibuilds.utils.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.IOException;

public class PromptTreeNode extends CommandTreeNode {
    public PromptTreeNode(CommandTreeNode parent) {
        super(parent, "prompt");
        addParameter(new Parameter("<batch_size>", (explorer, list) -> {
            for (int j = 0; j <= 10; ++j) {
                list.add("" + j);
            }
        }));
        addParameter(Parameter.PROMPT);

    }


    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            if (AIBuilds.plugin.configManager.APIToken == null) {
                Message.NO_API_TOKEN.format().send(sender);
                return CommandResult.FAILURE;
            }
            if (args.length < 3)
                return CommandResult.THROW_USAGE;
            Player player = (Player) sender;

            int batchSize;
            try {
                batchSize = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + args[1] + " is not a valid number.");
                return CommandResult.FAILURE;
            }
            if (batchSize <= 0 || batchSize > AIBuilds.plugin.configManager.maxBatchSize) {
                sender.sendMessage(ChatColor.RED + "Batch size must be between 1 and " + AIBuilds.plugin.configManager.maxBatchSize + ".");
                return CommandResult.FAILURE;
            }

            if (AIBuilds.plugin.tokenManager.getToken(player) < batchSize * AIBuilds.plugin.configManager.promptToken) {
                Message.NOT_ENOUGH_TOKEN.format("needed-tokens", batchSize * AIBuilds.plugin.configManager.promptToken, "owned-tokens", AIBuilds.plugin.tokenManager.getToken(player)).send(player);
                return CommandResult.FAILURE;
            }
            StringBuilder builder = new StringBuilder();

            for (int i = 2; i < args.length; i++) {
                builder.append(args[i]);
                builder.append(" ");
            }
            String prompt = builder.toString();
            int size = AIBuilds.plugin.configManager.defaultSize;
            Location location = player.getLocation().add(new Vector(size / 2 + 1, size / 2, size / 2 + 1));
            ConstructionHandler handler = new ConstructionHandler(player, location, size);
            CommunicationHandler client = new CommunicationHandler(handler);
            try {
                client.request(new String[]{prompt, "" + batchSize}, RequestType.SHAPE_PROMPT);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            AIBuilds.plugin.tokenManager.giveToken(player, -batchSize * AIBuilds.plugin.configManager.promptToken);
            Message.CURRENT_TOKEN.format("consumed-token", batchSize * AIBuilds.plugin.configManager.promptToken, "token", AIBuilds.plugin.tokenManager.getToken(player)).send(sender);
        }
        return CommandResult.SUCCESS;
    }
}

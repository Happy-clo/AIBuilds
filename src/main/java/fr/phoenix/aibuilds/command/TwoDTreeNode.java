package fr.phoenix.aibuilds.command;

import fr.phoenix.aibuilds.AIBuilds;
import fr.phoenix.aibuilds.command.objects.CommandTreeNode;
import fr.phoenix.aibuilds.command.objects.parameter.Parameter;
import fr.phoenix.aibuilds.communication.CommunicationHandler;
import fr.phoenix.aibuilds.communication.ConstructionHandler;
import fr.phoenix.aibuilds.communication.RequestType;
import fr.phoenix.aibuilds.utils.message.Message;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.IOException;

public class TwoDTreeNode extends CommandTreeNode {
    public TwoDTreeNode(CommandTreeNode parent) {
        super(parent, "2d");
        addParameter(Parameter.PROMPT);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            if (AIBuilds.plugin.configManager.APIToken == null) {
                Message.NO_API_TOKEN.format().send(sender);
                return CommandResult.FAILURE;
            }

            Player player = (Player) sender;

            if (AIBuilds.plugin.tokenManager.getToken(player) < AIBuilds.plugin.configManager.promptToken) {
                Message.NOT_ENOUGH_TOKEN.format("needed-tokens", AIBuilds.plugin.configManager.promptToken, "owned-tokens", AIBuilds.plugin.tokenManager.getToken(player)).send(player);
                return CommandResult.THROW_USAGE;
            }
            StringBuilder builder = new StringBuilder();

            for (int i = 1; i < args.length; i++) {
                builder.append(args[i]);
                builder.append(" ");
            }
            String prompt = builder.toString();
            int size = AIBuilds.plugin.configManager.defaultSize;
            Location location = player.getLocation().add(new Vector(size / 2 + 1, size / 2, size / 2 + 1));
            ConstructionHandler handler = new ConstructionHandler(player, location, size);
            CommunicationHandler client = new CommunicationHandler(handler);
            try {
                client.request(new String[]{prompt}, RequestType.TWOD);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            AIBuilds.plugin.tokenManager.giveToken(player, -AIBuilds.plugin.configManager.twoDToken);
            Message.CURRENT_TOKEN.format("consumed-token", AIBuilds.plugin.configManager.twoDToken, "token", AIBuilds.plugin.tokenManager.getToken(player)).send(sender);

        }
        return CommandResult.SUCCESS;
    }
}


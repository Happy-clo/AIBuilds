package fr.phoenix.aibuilds.command;

import fr.phoenix.aibuilds.AIBuilds;
import fr.phoenix.aibuilds.command.objects.CommandTreeNode;
import fr.phoenix.aibuilds.command.objects.parameter.Parameter;
import fr.phoenix.aibuilds.communication.CommunicationHandler;
import fr.phoenix.aibuilds.communication.ConstructionHandler;
import fr.phoenix.aibuilds.utils.message.Message;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.function.Supplier;

public class ImageTreeNode extends CommandTreeNode {
    public ImageTreeNode(CommandTreeNode parent) {
        super(parent, "image");
        addParameter(Parameter.QUALITY);
        addParameter(Parameter.IMAGE);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (AIBuilds.plugin.configManager.APIToken == null) {
            Message.NO_API_TOKEN.format().send(sender);
            return CommandResult.FAILURE;
        }
        if (!(sender instanceof Player))
            return CommandResult.FAILURE;
        Player player = (Player) sender;

        if (args.length < 2)
            return CommandResult.FAILURE;

        int tokenConsumed=AIBuilds.plugin.configManager.midToken;

        if (AIBuilds.plugin.tokenManager.getToken(player) < tokenConsumed) {
            Message.NOT_ENOUGH_TOKEN.format("needed-tokens", tokenConsumed, "owned-tokens", AIBuilds.plugin.tokenManager.getToken(player)).send(player);
            return CommandResult.FAILURE;
        }

        String imageURL = args[2];
        int size = AIBuilds.plugin.configManager.defaultSize;
        Location location = player.getLocation().add(new Vector(size / 2 + 1, size / 2, size / 2 + 1));
        ConstructionHandler handler = new ConstructionHandler(player, location, size,true);
        CommunicationHandler client = new CommunicationHandler(handler);
        try {
            client.requestImage(imageURL);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        AIBuilds.plugin.tokenManager.giveToken(player, -tokenConsumed);
        Message.CURRENT_TOKEN.format("consumed-token", tokenConsumed, "token", AIBuilds.plugin.tokenManager.getToken(player)).send(sender);
        return CommandResult.SUCCESS;
    }

}

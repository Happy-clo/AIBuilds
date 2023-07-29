package fr.phoenix.aibuilds.command.ThreeD;

import fr.phoenix.aibuilds.command.objects.CommandTreeNode;
import org.bukkit.command.CommandSender;

public class ShapETreeNode extends CommandTreeNode {

    public ShapETreeNode(CommandTreeNode parent) {
        super(parent, "shap-e");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }
}

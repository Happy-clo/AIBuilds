package fr.phoenix.aibuilds.command;

import fr.phoenix.aibuilds.command.objects.CommandTreeRoot;

public class AIBuildsTreeRoot extends CommandTreeRoot {


    public AIBuildsTreeRoot(String id, String permission) {
        super(id, permission);
        addChild(new TwoDTreeNode(this));
        addChild(new ImageTreeNode(this));;
        addChild(new PromptTreeNode(this));
        addChild(new ReloadTreeNode(this));
        addChild(new TokenTreeNode(this));
    }


}


package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class BlushCommand extends InteractionCommand {

    public BlushCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/qDcjxkC.gif",
            "https://i.imgur.com/bNZOp5E.gif",
            "https://i.imgur.com/1LpATjX.gif",
            "https://i.imgur.com/n9DOnX5.gif",
            "https://i.imgur.com/Yj0OukW.gif"
        );
    }

    @Override
    public String getName() {
        return "Blush Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("blush", "blushes");
    }
}

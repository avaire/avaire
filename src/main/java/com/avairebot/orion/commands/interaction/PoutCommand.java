package com.avairebot.orion.commands.interaction;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class PoutCommand extends InteractionCommand {

    public PoutCommand(Orion orion) {
        super(orion, "pouts at");
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/LuJllaE.gif",
            "https://i.imgur.com/bUk20mW.gif",
            "https://i.imgur.com/gWGH3mW.gif",
            "https://i.imgur.com/DZEqNrV.gif",
            "https://i.imgur.com/IvRIGse.gif",
            "https://i.imgur.com/ZeWvVY5.gif",
            "https://i.imgur.com/GsMZGE5.gif",
            "https://i.imgur.com/3EidTZl.gif",
            "https://i.imgur.com/B6Dsfsi.gif",
            "https://i.imgur.com/793Xoz8.gif",
            "https://i.imgur.com/p2gQm0i.gif"
        );
    }

    @Override
    public String getName() {
        return "Pout Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("pouts", "pout");
    }
}

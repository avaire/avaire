package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class PoutCommand extends InteractionCommand {

    public PoutCommand(AvaIre avaire) {
        super(avaire);
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

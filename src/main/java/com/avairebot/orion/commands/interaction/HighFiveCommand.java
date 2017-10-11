package com.avairebot.orion.commands.interaction;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class HighFiveCommand extends InteractionCommand {

    public HighFiveCommand(Orion orion) {
        super(orion, "high fives");
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/IxECtIA.gif",
            "https://i.imgur.com/h9y9lzT.gif",
            "https://i.imgur.com/mcbqQAx.gif",
            "https://i.imgur.com/7IopcFX.gif",
            "https://i.imgur.com/CjFaqDP.gif"
        );
    }

    @Override
    public String getName() {
        return "High Five Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("highfive", "highfives");
    }
}

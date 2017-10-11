package com.avairebot.orion.commands.interaction;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class BiteCommand extends InteractionCommand {

    public BiteCommand(Orion orion) {
        super(orion, "bites");
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/qcPNLOH.gif",
            "https://i.imgur.com/t89dF2n.gif",
            "https://i.imgur.com/fPCg6Or.gif"
        );
    }

    @Override
    public String getName() {
        return "Bite Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("bite", "bites");
    }
}

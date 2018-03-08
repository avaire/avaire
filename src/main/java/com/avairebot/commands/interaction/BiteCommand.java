package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class BiteCommand extends InteractionCommand {

    public BiteCommand(AvaIre avaire) {
        super(avaire);
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

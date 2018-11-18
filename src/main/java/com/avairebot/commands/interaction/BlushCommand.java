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
            "https://i.imgur.com/m5q2jwP.gif",
            "https://i.imgur.com/8XzsQ8v.gif",
            "https://i.imgur.com/iCWneqB.gif",
            "https://i.imgur.com/etHFA8z.gif",
            "https://i.imgur.com/5Hs93Q7.gif"
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

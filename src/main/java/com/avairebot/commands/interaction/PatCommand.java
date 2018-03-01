package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class PatCommand extends InteractionCommand {

    public PatCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/J01NZCa.gif",
            "https://i.imgur.com/gGfSj84.gif",
            "https://i.imgur.com/17NzO6G.gif",
            "https://i.imgur.com/rGMvRNn.gif",
            "https://i.imgur.com/XuV9dUZ.gif"
        );
    }

    @Override
    public String getName() {
        return "Pat Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("pat", "pats");
    }
}

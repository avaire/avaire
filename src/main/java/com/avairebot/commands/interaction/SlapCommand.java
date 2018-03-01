package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class SlapCommand extends InteractionCommand {

    public SlapCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/omhZ2aQ.gif",
            "https://i.imgur.com/nSDYCNZ.gif",
            "https://i.imgur.com/bF9ruep.gif",
            "https://i.imgur.com/1aqwJq4.gif",
            "https://i.imgur.com/xQYw5E6.gif"
        );
    }

    @Override
    public String getName() {
        return "Slap Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("slap", "slaps");
    }
}

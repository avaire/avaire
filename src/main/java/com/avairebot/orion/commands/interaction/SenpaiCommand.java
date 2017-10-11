package com.avairebot.orion.commands.interaction;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SenpaiCommand extends InteractionCommand {

    public SenpaiCommand(Orion orion) {
        super(orion, "notice me");
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/7X0TzmR.gif",
            "https://i.imgur.com/j4vdwZh.gif",
            "https://i.imgur.com/e1MXv4L.gif"
        );
    }

    @Override
    public String getName() {
        return "Senpai Command";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("senpai");
    }
}

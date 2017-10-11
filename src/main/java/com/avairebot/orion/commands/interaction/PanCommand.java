package com.avairebot.orion.commands.interaction;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PanCommand extends InteractionCommand {

    public PanCommand(Orion orion) {
        super(orion, "**%s** just hit **%s** with a pan!", true);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
                "https://i.imgur.com/4x3JLai.gif",
                "https://i.imgur.com/3IltdKo.gif",
                "https://i.imgur.com/ruFqQkT.gif"
        );
    }

    @Override
    public String getName() {
        return "Pan Command";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("pan");
    }
}

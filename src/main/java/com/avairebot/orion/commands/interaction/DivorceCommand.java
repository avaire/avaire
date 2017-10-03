package com.avairebot.orion.commands.interaction;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DivorceCommand extends InteractionCommand {

    public DivorceCommand(Orion orion) {
        super(orion, "divorces");
    }

    @Override
    public List<String> getInteractionImages() {
        return Collections.singletonList("https://i.imgur.com/VvFzH9D.gif");
    }

    @Override
    public String getName() {
        return "Divorce Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("divorce", "divorces");
    }
}

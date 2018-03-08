package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DivorceCommand extends InteractionCommand {

    public DivorceCommand(AvaIre avaire) {
        super(avaire);
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

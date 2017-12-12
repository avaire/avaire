package com.avairebot.orion.commands.interaction;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class HugCommand extends InteractionCommand {

    public HugCommand(Orion orion) {
        super(orion, "hugs");
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/aBdIEEu.gif",
            "https://i.imgur.com/03grRGj.gif",
            "https://i.imgur.com/EuIBiLi.gif",
            "https://i.imgur.com/8KgVR9j.gif",
            "https://i.imgur.com/ZepPo0t.gif"
        );
    }

    @Override
    public String getName() {
        return "Hug Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("hug", "hugs");
    }
}

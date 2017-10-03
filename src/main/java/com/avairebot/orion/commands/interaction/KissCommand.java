package com.avairebot.orion.commands.interaction;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class KissCommand extends InteractionCommand {

    public KissCommand(Orion orion) {
        super(orion, "kisses");
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
                "https://i.imgur.com/B0qeWUA.gif",
                "https://i.imgur.com/CEyl2rH.gif",
                "https://i.imgur.com/gYkehjd.gif",
                "https://i.imgur.com/k2nNews.gif",
                "https://i.imgur.com/ehOIkBq.gif"
        );
    }

    @Override
    public String getName() {
        return "Kiss Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("kiss", "kisses");
    }
}

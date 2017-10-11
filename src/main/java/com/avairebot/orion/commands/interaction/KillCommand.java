package com.avairebot.orion.commands.interaction;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class KillCommand extends InteractionCommand {

    public KillCommand(Orion orion) {
        super(orion, "wants to kill");
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
                "https://i.imgur.com/8Ryi8xQ.gif",
                "https://i.imgur.com/hGCGVmZ.gif",
                "https://i.imgur.com/mHTunac.gif",
                "https://i.imgur.com/xWkNtE5.gif",
                "https://i.imgur.com/1gVPkev.gif"
        );
    }

    @Override
    public String getName() {
        return "Kill Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("kill", "kills");
    }
}

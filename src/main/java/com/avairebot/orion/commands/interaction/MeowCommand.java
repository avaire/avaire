package com.avairebot.orion.commands.interaction;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.CommandPriority;
import com.avairebot.orion.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class MeowCommand extends InteractionCommand {

    public MeowCommand(Orion orion) {
        super(orion, "meows at");
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/PJUZAXX.gif",
            "https://i.imgur.com/WtVaAGp.gif",
            "https://i.imgur.com/E3JpP7e.gif",
            "https://i.imgur.com/wAD0tqK.gif",
            "https://i.imgur.com/W7qSt0G.gif"
        );
    }

    @Override
    public String getName() {
        return "Meow Command :3";
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.HIDDEN;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("meows", "meow");
    }
}

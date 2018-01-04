package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class MeowCommand extends InteractionCommand {

    public MeowCommand(AvaIre avaire) {
        super(avaire, "meows at");
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

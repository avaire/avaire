package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class MeowCommand extends InteractionCommand {

    public MeowCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/PJUZAXX.gif",
            "https://i.imgur.com/WtVaAGp.gif",
            "https://i.imgur.com/E3JpP7e.gif",
            "https://i.imgur.com/wAD0tqK.gif",
            "https://i.imgur.com/W7qSt0G.gif",
            "https://i.imgur.com/PLwWhJz.gif",
            "https://i.imgur.com/ozn4UyC.gif",
            "https://i.imgur.com/DbvyqfH.gif",
            "https://i.imgur.com/WsLsRkd.gif",
            "https://i.imgur.com/fWLT1Q0.gif",
            "https://i.imgur.com/zyBznwg.gif"
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

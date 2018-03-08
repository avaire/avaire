package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class TickleCommand extends InteractionCommand {

    public TickleCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/qrjkj6d.gif",
            "https://i.imgur.com/fA3o4p4.gif",
            "https://i.imgur.com/AfobFAt.gif",
            "https://i.imgur.com/ExUivDr.gif",
            "https://i.imgur.com/osnnduz.gif",
            "https://i.imgur.com/hCbjjQ1.gif"
        );
    }

    @Override
    public String getName() {
        return "Tickle Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("tickle", "tickles");
    }
}

package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class EatCommand extends InteractionCommand {

    public EatCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/cVOqRpf.gif",
            "https://i.imgur.com/Ec7OmbS.gif",
            "https://i.imgur.com/zhwLNTC.gif",
            "https://i.imgur.com/ShhXrms.gif",
            "https://i.imgur.com/7eUwFc0.gif",
            "https://i.imgur.com/sxB3LZt.gif",
            "https://i.imgur.com/y7GdCeq.gif",
            "https://i.imgur.com/pFl0PkY.gif",
            "https://i.imgur.com/ux8rVDC.gif"
        );
    }

    @Override
    public String getName() {
        return "Eat Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("eats", "eat", "eats-with");
    }
}

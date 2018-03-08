package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class CuddleCommand extends InteractionCommand {

    public CuddleCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/HqFckvG.gif",
            "https://i.imgur.com/pH8TTOd.gif",
            "https://i.imgur.com/3SqJTbV.gif"
        );
    }

    @Override
    public String getName() {
        return "Cuddle Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("cuddle", "cuddles");
    }
}

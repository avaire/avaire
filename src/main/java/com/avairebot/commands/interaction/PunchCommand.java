package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class PunchCommand extends InteractionCommand {

    public PunchCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/0baM7Wd.gif",
            "https://i.imgur.com/fVPGbo5.gif",
            "https://i.imgur.com/vHRVMe9.gif",
            "https://i.imgur.com/g3MH1Bs.gif",
            "https://i.imgur.com/7SbOwl4.gif"
        );
    }

    @Override
    public String getName() {
        return "Punch Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("punch", "punches");
    }
}

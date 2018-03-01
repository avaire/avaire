package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class HelloCommand extends InteractionCommand {

    public HelloCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/odFyo1Q.gif",
            "https://i.imgur.com/KyQN1RR.gif",
            "https://i.imgur.com/LDxsTxv.gif",
            "https://i.imgur.com/czWxIkd.gif",
            "https://i.imgur.com/DIeaddE.gif",
            "https://i.imgur.com/lUfKcOK.gif",
            "https://i.imgur.com/bwwasl2.gif",
            "https://i.imgur.com/cYsTgNK.gif"
        );
    }

    @Override
    public String getName() {
        return "Hello Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("hello", "herro", "hi");
    }
}

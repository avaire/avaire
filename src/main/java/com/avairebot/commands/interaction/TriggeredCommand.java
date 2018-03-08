package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TriggeredCommand extends InteractionCommand {

    public TriggeredCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/HZ3nuUp.gif",
            "https://i.imgur.com/PL71evt.gif",
            "https://i.imgur.com/ISGZQzD.gif",
            "https://i.imgur.com/mE7JmCq.gif",
            "https://i.imgur.com/yxl7WzG.gif",
            "https://i.imgur.com/ssJNnyS.gif"
        );
    }

    @Override
    public String getName() {
        return "Triggered Command";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("triggered");
    }
}

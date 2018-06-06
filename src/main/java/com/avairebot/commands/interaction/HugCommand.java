package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class HugCommand extends InteractionCommand {

    public HugCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/aBdIEEu.gif",
            "https://i.imgur.com/03grRGj.gif",
            "https://i.imgur.com/EuIBiLi.gif",
            "https://i.imgur.com/8KgVR9j.gif",
            "https://i.imgur.com/ZepPo0t.gif",
            "https://i.imgur.com/iIsFQ3q.gif",
            "https://i.imgur.com/XHhFoR1.gif",
            "https://i.imgur.com/psGdps5.gif",
            "https://i.imgur.com/OPKBDeA.gif",
            "https://i.imgur.com/D0GABc2.gif",
            "https://i.imgur.com/LtVBCX3.gif",
            "https://i.imgur.com/o8JQtVL.gif"
        );
    }

    @Override
    public String getName() {
        return "Hug Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("hug", "hugs");
    }
}

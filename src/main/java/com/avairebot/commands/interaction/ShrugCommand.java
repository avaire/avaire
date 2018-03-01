package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.InteractionCommand;
import com.avairebot.utilities.RandomUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShrugCommand extends InteractionCommand {

    public ShrugCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public List<String> getInteractionImages() {
        if (RandomUtil.getInteger(100) == 0) {
            return Collections.singletonList("https://i.imgur.com/vqv3gJb.gif");
        }

        return Arrays.asList(
            "https://i.imgur.com/gX6MK7x.gif",
            "https://i.imgur.com/eaETYOu.gif",
            "https://i.imgur.com/xW53ysv.gif",
            "https://i.imgur.com/SJCfRNj.gif",
            "https://i.imgur.com/hFD877M.gif"
        );
    }

    @Override
    public String getName() {
        return "Shrug Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("shrugs", "shrug");
    }
}

package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class InviteCommand extends AbstractCommand {
    public InviteCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Invite Command";
    }

    @Override
    public String getDescription() {
        return "Returns a link that can be used to invite to bot to other servers.";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("invite", "join");
    }

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args) {
        MessageFactory.makeInfo(e.getMessage(),
                "You can invite me to your server by [clicking here](%s) or use the link below:\n\n%s",
                orion.config.botAuth().getOAuth(), orion.config.botAuth().getOAuth())
                .queue();
    }
}

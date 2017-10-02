package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InviteCommand extends Command {
    public InviteCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Invite Command";
    }

    @Override
    public String getDescription() {
        return "Returns a link that can be used to invite the bot to other servers.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Gives you an invite link that can be used to invite Orion to servers.");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("invite", "join");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        MessageFactory.makeInfo(message,
                "You can invite me to your server by [clicking here](%s) or use the link below:\n\n%s",
                orion.config.botAuth().getOAuth(), orion.config.botAuth().getOAuth())
                .queue();

        return true;
    }
}

package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InviteCommand extends Command {

    public InviteCommand(AvaIre avaire) {
        super(avaire);
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
        return Collections.singletonList("`:command` - Gives you an invite link that can be used to invite AvaIre to servers.");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("invite", "join");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        context.makeInfo(context.i18n("message") + "\n\n:oauth")
            .set("oauth", avaire.getConfig().getString("discord.oauth"))
            .queue();
        return true;
    }
}

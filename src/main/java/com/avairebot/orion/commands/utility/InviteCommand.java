package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Arrays;
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
        return "Returns a link that can be used to invite to bot to other servers.";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("invite", "join");
    }

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args) {
        e.getMessage().getChannel().sendMessage(new EmbedBuilder()
                .setColor(Color.decode("#3A71C1"))
                .setDescription(new StringBuilder()
                        .append("You can invite me to your server by [clicking here](")
                        .append(this.orion.config.botAuth().getOAuth())
                        .append(") or use the link below:\n\n")
                        .append(this.orion.config.botAuth().getOAuth()).toString()
                ).build()).queue();
    }
}

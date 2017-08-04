package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class PingCommand extends Command {

    public PingCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Ping Command";
    }

    @Override
    public String getDescription() {
        return "Can be used to check if the bot is still alive.";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("ping");
    }

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args) {
        e.getMessage().getChannel().sendMessage(new EmbedBuilder()
                .setColor(Color.decode("#3A71C1"))
                .setDescription("Pong! Time taken " + e.getJDA().getPing() + " ms!")
                .build()).queue();
    }
}

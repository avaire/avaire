package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.List;

public class ServerIdCommand extends Command {

    public ServerIdCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Server ID Command";
    }

    @Override
    public String getDescription() {
        return "Shows the ID of the server the command was ran in.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return null;
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("serverid", "sid");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        MessageFactory.makeSuccess(message, "<@%s> :id: of this server is `%s`",
            message.getAuthor().getId(),
            message.getGuild().getId()
        ).queue();
        return true;
    }
}

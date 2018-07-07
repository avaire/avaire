package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ServerIdCommand extends Command {

    public ServerIdCommand(AvaIre avaire) {
        super(avaire, false);
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
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(ServerInfoCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("serverid", "sid");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        context.makeSuccess(context.i18n("message"))
            .queue();

        return true;
    }
}

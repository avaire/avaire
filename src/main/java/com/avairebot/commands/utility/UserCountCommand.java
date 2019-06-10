package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UserCountCommand extends Command
{

    public UserCountCommand(AvaIre avaire)
    {
        super(avaire);
    }

    /**
     * Gets the command name, this is used in help and error
     * messages as the title as well as log messages.
     *
     * @return Never-null, the command name.
     */
    @Override
    public String getName() {
        return "User Count Command";
    }

    @Override
    public String getDescription() {
        return "Returns the number of users in the current guild.";
    }


    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Returns the number of users in this guild" );

    }

    /**
     * Gets am immutable list of command triggers that can be used to invoke the current
     * command, the first index in the list will be used when the `:command` placeholder
     * is used in {@link #getDescription(CommandContext)} or {@link #getUsageInstructions()} methods.
     *
     * @return An immutable list of command triggers that should invoked the command.
     */
    @Override
    public List<String> getTriggers()
    {
        return Collections.singletonList("usercount");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList(
            "throttle:guild,1,4");
    }

    /**
     * The command executor, this method is invoked by the command handler
     * and the middleware stack when a user sends a message matching the
     * commands prefix and one of its command triggers.
     *
     * @param context The command message context generated using the
     *                JDA message event that invoked the command.
     * @param args    The arguments given to the command, if no arguments was given the array will just be empty.
     * @return true on success, false on failure.
     */
    @Override
    public boolean onCommand(CommandMessage context, String[] args)
    {
        int userCount = context.guild.getMembers().size();

        context.makeInfo(context.i18n("message",userCount)).queue();
        return true;
    }
}

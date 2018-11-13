package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandContext;
import net.dv8tion.jda.core.entities.Member;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class JoinDateCommand extends Command
{
    public JoinDateCommand(AvaIre avaire)
    {
        super(avaire, false);
    }


    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command`");
    }

    /**
     * Gets the command name, this is used in help and error
     * messages as the title as well as log messages.
     *
     * @return Never-null, the command name.
     */
    @Override
    public String getName() {
        return "Join Date Command";
    }

    @Override
    public String getDescription()
    {
        return "Retrieves the date the user joined the server. ";
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
        return Arrays.asList("joindate","joinedat");
    }

    /**
     * The command executor, this method is invoked by the command handler
     * and the middleware stack when a user sends a message matching the
     * commands prefix and one of its command triggers.
     *
     * @param context The JDA message object from the message received event.
     * @param args    The arguments given to the command, if no arguments was given the array will just be empty.
     * @return true on success, false on failure.
     */
    @Override
    public boolean onCommand(CommandMessage context, String[] args)
    {
        Member member = context.member;

        OffsetDateTime time = member.getJoinDate();

        context.getMessageChannel().sendMessage(member.getEffectiveName() + " joined on " + time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).queue();

        return true;
    }
}

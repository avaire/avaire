package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class BlushCommand extends InteractionCommand
{

    public BlushCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public List<String> getInteractionImages()
    {
        return Arrays.asList("https://i.imgur.com/qDcjxkC.gif","https://i.imgur.com/bNZOp5E.gif",
                                "https://i.imgur.com/1LpATjX.gif","https://i.imgur.com/n9DOnX5.gif"
                                ,"https://i.imgur.com/Yj0OukW.gif");
    }

    /**
     * Gets the command name, this is used in help and error
     * messages as the title as well as log messages.
     *
     * @return Never-null, the command name.
     */
    @Override
    public String getName()
    {
        return "Blush Command";
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
        return Arrays.asList("blush", "blushes");
    }
}

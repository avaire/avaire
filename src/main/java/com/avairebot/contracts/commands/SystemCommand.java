package com.avairebot.contracts.commands;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandPriority;

import java.util.Collections;
import java.util.List;

public abstract class SystemCommand extends Command {

    /**
     * Creates the given command instance by calling {@link #SystemCommand(AvaIre, boolean)} with allowDM set to true.
     *
     * @param avaire The AvaIre class instance.
     */
    public SystemCommand(AvaIre avaire) {
        this(avaire, true);
    }

    /**
     * Creates the given command instance with the given
     * AvaIre instance and the allowDM settings.
     *
     * @param avaire  The AvaIre class instance.
     * @param allowDM Determines if the command can be used in DMs.
     */
    public SystemCommand(AvaIre avaire, boolean allowDM) {
        super(avaire, allowDM);
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("isBotAdmin");
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.SYSTEM;
    }
}

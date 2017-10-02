package com.avairebot.orion.contracts.commands;

import com.avairebot.orion.Orion;

import java.util.Collections;
import java.util.List;

public abstract class SystemCommand extends Command {

    /**
     * Creates the given command instance by calling {@link #SystemCommand(Orion, boolean)} with allowDM set to true.
     *
     * @param orion The Orion class instance.
     */
    public SystemCommand(Orion orion) {
        this(orion, true);
    }

    /**
     * Creates the given command instance with the given
     * Orion instance and the allowDM settings.
     *
     * @param orion   The Orion class instance.
     * @param allowDM Determines if the command can be used in DMs.
     */
    public SystemCommand(Orion orion, boolean allowDM) {
        super(orion, allowDM);
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("isBotAdmin");
    }
}

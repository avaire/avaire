package com.avairebot.orion.contracts.commands;

import com.avairebot.orion.Orion;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCommand {
    protected final Orion orion;
    protected final boolean allowDM;

    public AbstractCommand(Orion orion) {
        this(orion, false);
    }

    public AbstractCommand(Orion orion, boolean allowDM) {
        this.orion = orion;
        this.allowDM = allowDM;
    }

    public abstract String getName();

    public abstract String getDescription();

    public abstract List<String> getUsageInstructions();

    public abstract List<String> getTriggers();

    public List<String> getMiddleware() {
        return new ArrayList<>();
    }

    public boolean isAllowedInDM() {
        return allowDM;
    }

    public abstract boolean onCommand(Message message, String[] args);
}

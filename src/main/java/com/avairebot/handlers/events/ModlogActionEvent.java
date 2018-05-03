package com.avairebot.handlers.events;

import com.avairebot.modules.ModlogModule;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;

import javax.annotation.Nullable;

public class ModlogActionEvent extends Event {

    private final ModlogModule.ModlogAction action;
    private final int caseId;

    public ModlogActionEvent(JDA api, ModlogModule.ModlogAction action, int caseId) {
        super(api);

        this.action = action;
        this.caseId = caseId;
    }

    @Nullable
    public User getTarget() {
        return action.getTarget();
    }

    @Nullable
    public String getTargetStringified() {
        return action.getStringifiedTarget();
    }

    public User getModerator() {
        return action.getModerator();
    }

    public String getModeratorStringified() {
        return action.getStringifiedModerator();
    }

    @Nullable
    public String getReason() {
        return action.getMessage();
    }

    public ModlogModule.ModlogType getType() {
        return action.getType();
    }

    public int getCaseId() {
        return caseId;
    }
}

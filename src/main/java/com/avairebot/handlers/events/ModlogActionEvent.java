/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.handlers.events;

import com.avairebot.modlog.ModlogAction;
import com.avairebot.modlog.ModlogType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;

import javax.annotation.Nullable;

public class ModlogActionEvent extends Event {

    private final ModlogAction action;
    private final int caseId;

    public ModlogActionEvent(JDA api, ModlogAction action, int caseId) {
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

    public ModlogType getType() {
        return action.getType();
    }

    public int getCaseId() {
        return caseId;
    }
}

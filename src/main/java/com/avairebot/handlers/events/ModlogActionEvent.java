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

@SuppressWarnings("unused")
public class ModlogActionEvent extends Event {

    private final ModlogAction action;
    private final int caseId;

    /**
     * Creates a new modlog action event.
     *
     * @param api    The JDA api(shard) that the event should be triggered on.
     * @param action The modlog action that is happening.
     * @param caseId The case ID for the given modlog event.
     */
    public ModlogActionEvent(JDA api, ModlogAction action, int caseId) {
        super(api);

        this.action = action;
        this.caseId = caseId;
    }

    /**
     * Gets the target for the modlog action.
     *
     * @return The target for the modlog action.
     */
    @Nullable
    public User getTarget() {
        return action.getTarget();
    }

    /**
     * Gets the stringified version of the target, containing the users
     * username and discriminator, as well as a mention in brackets.
     *
     * @return The stringified version of the target.
     */
    @Nullable
    public String getTargetStringified() {
        return action.getStringifiedTarget();
    }

    /**
     * Gets the moderator for the modlog action.
     *
     * @return The moderator for the modlog action.
     */
    public User getModerator() {
        return action.getModerator();
    }

    /**
     * Gets the stringified version of the moderator, containing the users
     * username and discriminator, as well as a mention in brackets.
     *
     * @return The stringified version of the moderator.
     */
    public String getModeratorStringified() {
        return action.getStringifiedModerator();
    }

    /**
     * Gets the reason for the modlog action.
     *
     * @return The reason for the modlog action.
     */
    @Nullable
    public String getReason() {
        return action.getMessage();
    }

    /**
     * Gets the type of modlog action that is being preformed.
     *
     * @return The type of modlog action that is being preformed.
     */
    public ModlogType getType() {
        return action.getType();
    }

    /**
     * Gets the case ID for the modlog action, the case ID can be used later
     * by moderators to change the reason for a given modlog action.
     *
     * @return The case ID for the modlog action.
     */
    public int getCaseId() {
        return caseId;
    }
}

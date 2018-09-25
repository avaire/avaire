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

package com.avairebot.modlog;

import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModlogAction {

    private final User moderator;
    private final User target;
    private final long targetId;

    private ModlogType type;
    private String message = null;

    /**
     * Creates a new modlog action with the given
     * action type, moderator, and target.
     *
     * @param action    The type of modlog action that is being preformed.
     * @param moderator The moderator for the modlog action.
     * @param target    The target of the modlog action.
     */
    public ModlogAction(@Nonnull ModlogType action, @Nonnull User moderator, User target) {
        this(action, moderator, target, null);
    }

    /**
     * Creates a new modlog action with the given action
     * type, moderator, target, and message/reason.
     *
     * @param action    The type of modlog action that is being preformed.
     * @param moderator The moderator for the modlog action.
     * @param target    The target of the modlog action.
     */
    public ModlogAction(@Nonnull ModlogType action, @Nonnull User moderator, User target, @Nullable String message) {
        this.moderator = moderator;
        this.target = target;
        this.type = action;
        this.message = message;

        this.targetId = target == null ? 0L : target.getIdLong();
    }

    /**
     * Creates a new modlog action with the given action
     * type, moderator, target ID, and message/reason.
     *
     * @param action    The type of modlog action that is being preformed.
     * @param moderator The moderator for the modlog action.
     * @param targetId  The target ID of the modlog action.
     */
    public ModlogAction(@Nonnull ModlogType action, @Nonnull User moderator, long targetId, @Nullable String message) {
        this.moderator = moderator;
        this.type = action;
        this.message = message;

        this.target = null;
        this.targetId = targetId;
    }

    /**
     * Gets the moderator for the modlog action.
     *
     * @return The moderator for the modlog action.
     */
    public User getModerator() {
        return moderator;
    }

    /**
     * Gets the stringified version of the moderator, containing the users
     * username and discriminator, as well as a mention in brackets.
     *
     * @return The stringified version of the moderator.
     */
    public String getStringifiedModerator() {
        return stringifyUser(moderator);
    }

    /**
     * Gets the target for the modlog action.
     *
     * @return The target for the modlog action.
     */
    public User getTarget() {
        return target;
    }

    /**
     * Gets the stringified version of the target, containing the users
     * username and discriminator, as well as a mention in brackets.
     *
     * @return The stringified version of the target.
     */
    public String getStringifiedTarget() {
        if (target == null && targetId > 0) {
            return String.valueOf(targetId);
        }
        return stringifyUser(target);
    }

    /**
     * The ID of the target the modlog action was invoked for, if no target was
     * provided for the modlog action, the target ID will be 0, if the target
     * is a user but they're not on the server at the time, the target might
     * be null but the target ID will contain the ID of the target.
     *
     * @return The ID of the target the modlog action was invoked for.
     */
    public long getTargetId() {
        return targetId;
    }

    /**
     * Gets the type of modlog action that is being preformed.
     *
     * @return The type of modlog action that is being preformed.
     */
    public ModlogType getType() {
        return type;
    }

    /**
     * Sets the type of modlog action that should be preformed.
     *
     * @param type The type of modlog action that should be preformed.
     */
    public void setType(ModlogType type) {
        this.type = type;
    }

    /**
     * Gets the message/reason for the modlog action.
     *
     * @return The message for the modlog action.
     */
    @Nullable
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message that should be used for the modlog action.
     *
     * @param message The message that should be used for the modlog action.
     */
    public void setMessage(@Nullable String message) {
        this.message = message;
    }

    private String stringifyUser(User user) {
        if (user == null) {
            return "";
        }
        return user.getName() + "#" + user.getDiscriminator() + " (" + user.getAsMention() + ")";
    }
}

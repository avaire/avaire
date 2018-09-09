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

public class ModlogAction {

    private final User moderator;
    private final User target;

    private ModlogType type;
    private String message = null;

    public ModlogAction(@Nonnull ModlogType action, @Nonnull User moderator, User target) {
        this(action, moderator, target, null);
    }

    public ModlogAction(@Nonnull ModlogType action, @Nonnull User moderator, User target, String message) {
        this.moderator = moderator;
        this.target = target;
        this.type = action;
        this.message = message;
    }

    public User getModerator() {
        return moderator;
    }

    public String getStringifiedModerator() {
        return stringifyUser(moderator);
    }

    public User getTarget() {
        return target;
    }

    public String getStringifiedTarget() {
        return stringifyUser(target);
    }

    public ModlogType getType() {
        return type;
    }

    public void setType(ModlogType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String stringifyUser(User user) {
        if (user == null) {
            return "";
        }
        return user.getName() + "#" + user.getDiscriminator() + " (" + user.getAsMention() + ")";
    }
}

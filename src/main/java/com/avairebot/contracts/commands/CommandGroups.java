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

package com.avairebot.contracts.commands;

import javax.annotation.Nonnull;

public enum CommandGroups implements CommandGroup {

    MODERATION("Moderation"),
    COMMAND_CUSTOMIZATION("Command Customization"),
    LEVEL_AND_EXPERIENCE("Level & Experience"),
    ROLE_ASSIGNMENTS("Role Assignments"),
    JOIN_LEAVE_MESSAGES("Join/Leave Messages"),
    INTERACTIONS("Interactions"),
    INFORMATION("Informative"),
    BOT_INFORMATION("Bot Information"),
    MUSIC_QUEUE("Music Queue"),
    MUSIC_TRACK_MODIFIER("Track Modifier"),
    MUSIC_START_PLAYING("Start playing"),
    MUSIC_SETTINGS("Music Settings"),
    MUSIC_SKIP("Skip Track"),
    MISCELLANEOUS("Misc");

    private final String name;

    CommandGroups(String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }
}

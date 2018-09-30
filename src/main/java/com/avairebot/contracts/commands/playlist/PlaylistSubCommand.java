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

package com.avairebot.contracts.commands.playlist;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.music.PlaylistCommand;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlaylistTransformer;

public abstract class PlaylistSubCommand {

    protected final AvaIre avaire;
    protected final PlaylistCommand command;

    public PlaylistSubCommand(AvaIre avaire, PlaylistCommand command) {
        this.avaire = avaire;
        this.command = command;
    }

    public final boolean onCommand(CommandMessage context, String[] args, GuildTransformer guild, Object object) {
        if (object instanceof PlaylistTransformer) {
            return onCommand(context, args, guild, (PlaylistTransformer) object);
        }

        return object instanceof Collection && onCommand(context, args, guild, (Collection) object);

    }

    public boolean onCommand(CommandMessage context, String[] args, GuildTransformer guild, PlaylistTransformer playlist) {
        return false;
    }

    public boolean onCommand(CommandMessage context, String[] args, GuildTransformer guild, Collection playlists) {
        return false;
    }
}

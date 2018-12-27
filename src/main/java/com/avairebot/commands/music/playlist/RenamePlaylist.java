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

package com.avairebot.commands.music.playlist;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.music.PlaylistCommand;
import com.avairebot.contracts.commands.playlist.PlaylistSubCommand;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.controllers.PlaylistController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlaylistTransformer;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class RenamePlaylist extends PlaylistSubCommand {

    public RenamePlaylist(AvaIre avaire, PlaylistCommand command) {
        super(avaire, command);
    }

    public boolean onCommand(CommandMessage context, String[] args, GuildTransformer guild, PlaylistTransformer playlist, Collection playlists) {
        String name = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();

        if (name.length() == 0) {
            context.makeWarning(context.i18n("invalidFormat"))
                .set("command", command.generateCommandTrigger(context.getMessage()) + " " + playlist.getName() + " renameto <new name>")
                .set("type", "new name")
                .queue();

            return false;
        }

        name = name.split(" ")[0];

        List<DataRow> playlistItems = playlists.whereLoose("name", name);
        if (!playlistItems.isEmpty()) {
            context.makeInfo(context.i18n("renamingPlaylistAlreadyExists"))
                .set("oldplaylist", playlist.getName())
                .set("playlist", name)
                .queue();

            return false;
        }

        String oldPlaylist = playlist.getName();
        playlist.setName(name);

        try {
            avaire.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                .where("id", playlist.getId()).andWhere("guild_id", context.getGuild().getId())
                .update(statement -> {
                    statement.set("name", playlist.getName(), true);
                });

            PlaylistController.forgetCache(context.getGuild().getIdLong());

            context.makeSuccess(context.i18n("playlistRenamed"))
                .set("oldplaylist", oldPlaylist)
                .set("playlist", playlist.getName())
                .queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            context.makeError(context.i18n("failedToSavePlaylist", e.getMessage())).queue();
        }

        return false;
    }
}

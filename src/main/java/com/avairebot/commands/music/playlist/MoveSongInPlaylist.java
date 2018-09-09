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
import com.avairebot.database.controllers.PlaylistController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlaylistTransformer;
import com.avairebot.utilities.NumberUtil;

import java.sql.SQLException;
import java.util.Collections;

public class MoveSongInPlaylist extends PlaylistSubCommand {

    public MoveSongInPlaylist(AvaIre avaire, PlaylistCommand command) {
        super(avaire, command);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args, GuildTransformer guild, PlaylistTransformer playlist) {
        if (!isValidRequest(context, args, playlist)) {
            return false;
        }

        try {
            int currentPosition = NumberUtil.parseInt(args[2]) - 1;
            int newPosition = NumberUtil.parseInt(args[3]) - 1;

            if (currentPosition == newPosition) {
                context.makeWarning(context.i18n("sameIdsGiven"))
                    .set("command", command.generateCommandTrigger(context.getMessage()) + " " + playlist.getName() + " move <song id> <new position>")
                    .queue();

                return false;
            }

            if (currentPosition < 0 || currentPosition >= playlist.getSongs().size()) {
                context.makeWarning(context.i18n("invalidIdGiven"))
                    .set("command", command.generateCommandTrigger(context.getMessage()) + " " + playlist.getName() + " move <song id> <new position>")
                    .set("type", currentPosition < 0 ? "low" : "high")
                    .queue();

                return false;
            }

            if (newPosition < 0 || newPosition >= playlist.getSongs().size()) {
                context.makeWarning(context.i18n("invalidIdGiven"))
                    .set("command", command.generateCommandTrigger(context.getMessage()) + " " + playlist.getName() + " move :songId <new position>")
                    .set("type", newPosition < 0 ? "low" : "high")
                    .set("songId", currentPosition + 1)
                    .queue();

                return false;
            }

            Collections.<PlaylistTransformer.PlaylistSong>swap(
                playlist.getSongs(), currentPosition, newPosition
            );

            avaire.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                .where("id", playlist.getId()).andWhere("guild_id", context.getGuild().getId())
                .update(statement -> {
                    statement.set("songs", AvaIre.gson.toJson(playlist.getSongs()), true);
                    statement.set("amount", playlist.getSongs().size());
                });

            PlaylistController.forgetCache(context.getGuild().getIdLong());

            PlaylistTransformer.PlaylistSong replacedSong = playlist.getSongs().get(currentPosition);
            PlaylistTransformer.PlaylistSong selectedSong = playlist.getSongs().get(newPosition);

            context.makeSuccess(context.i18n("swapSongInPlaylist"))
                .set("selectedTitle", selectedSong.getTitle())
                .set("selectedLink", selectedSong.getLink())
                .set("replacedTitle", replacedSong.getTitle())
                .set("replacedLink", replacedSong.getLink())
                .set("playlist", playlist.getName())
                .queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean isValidRequest(CommandMessage context, String[] args, PlaylistTransformer playlist) {
        if (args.length < 4) {
            context.makeWarning(context.i18n("invalidFormat"))
                .set("command", command.generateCommandTrigger(context.getMessage()) + " " + playlist.getName() + " move <song id> <new position>")
                .set("type", args.length < 3 ? "song id" : "new position")
                .queue();

            return false;
        }

        if (playlist.getSongs().isEmpty()) {
            context.makeWarning(context.i18n("playlistIsAlreadyEmpty"))
                .set("playlist", playlist.getName())
                .queue();

            return false;
        }

        return true;
    }
}

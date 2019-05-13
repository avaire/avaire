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
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.music.PlaylistCommand;
import com.avairebot.contracts.commands.playlist.PlaylistSubCommand;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlaylistTransformer;
import com.avairebot.utilities.NumberUtil;

import java.util.ArrayList;
import java.util.List;

public class SendSongsInPlaylist extends PlaylistSubCommand {

    public SendSongsInPlaylist(AvaIre avaire, PlaylistCommand command) {
        super(avaire, command);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args, GuildTransformer guild, PlaylistTransformer playlist) {
        if (playlist.getSongs().isEmpty()) {
            context.makeWarning(context.i18n("playlistIsEmpty"))
                .set("command", command.generateCommandTrigger(context.getMessage()) + " " + playlist.getName() + " add <song url>")
                .queue();

            return false;
        }

        SimplePaginator<PlaylistTransformer.PlaylistSong> paginator = new SimplePaginator<>(playlist.getSongs(), 10);
        if (args.length > 1) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[1], 1));
        }

        List<String> messages = new ArrayList<>();
        paginator.forEach((index, key, song) -> {
            messages.add(context.i18n("playlistSongLine",
                index + 1,
                song.getTitle(),
                song.getLink(),
                song.getDuration()
            ));
        });

        context.makeInfo(
            String.join("\n", messages) + "\n\n" + paginator.generateFooter(
                context.getGuild(),
                command.generateCommandTrigger(context.getMessage()) + " " + playlist.getName()
            )
        ).setTitle(":musical_note: " + playlist.getName()).queue();

        return true;
    }
}

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

package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.AudioTrackContainer;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RestActionUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RemoveSongFromQueueCommand extends Command {

    public RemoveSongFromQueueCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Remove Song From Queue";
    }

    @Override
    public String getDescription() {
        return "Removes a song from the music queue.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <song id>` Removes the song with the given ID from the queue."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("removesong", "songremove");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:normal",
            "throttle:user,2,4",
            "musicChannel"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MUSIC_QUEUE);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, context.i18n("error"));
        }

        int removeIndex = NumberUtil.parseInt(args[0], -1);
        if (removeIndex < 1) {
            return sendErrorMessage(context, context.i18n("mustBePositiveNumber"));
        }

        GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild());

        if (musicManager.getScheduler().getQueue().isEmpty()) {
            return sendErrorMessage(context, context.i18n("nothingToRemove", generateCommandPrefix(context.getMessage())));
        }

        if (removeIndex > musicManager.getScheduler().getQueue().size()) {
            return sendErrorMessage(context, context.i18n("tooHighNumberGiven"),
                NumberUtil.formatNicely(musicManager.getScheduler().getQueue().size())
            );
        }

        Iterator<AudioTrackContainer> iterator = musicManager.getScheduler().getQueue().iterator();

        int counter = 0;
        while (iterator.hasNext()) {
            AudioTrackContainer next = iterator.next();

            if (++counter != removeIndex) {
                continue;
            }

            AudioTrackInfo track = next.getAudioTrack().getInfo();
            context.makeInfo(context.i18n("success"))
                .set("song", String.format("[%s](%s)",
                    track.title, track.uri
                ))
                .queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES, null, RestActionUtil.ignore));

            iterator.remove();
            return true;
        }

        context.makeError(context.i18n("failedToRemoveSong"))
            .set("index", removeIndex)
            .queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES, null, RestActionUtil.ignore));

        return false;
    }
}

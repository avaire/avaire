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

public class RemoveMultipleFromQueueCommand extends Command {

    public RemoveMultipleFromQueueCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Remove Multiple Songs From Queue";
    }

    @Override
    public String getDescription() {
        return "Removes multiple songs from the music queue.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <start-song-id> - <end-song-id>` Removes the songs within the given IDs, inclusive, from the queue."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("removesongs", "songsremove");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:normal",
            "throttle:user,2,4",
            "musicChannel"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command 3-6` - Removes songs 3-6 in the queue.");
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

        int startIndex, endIndex;
        try {
            startIndex = NumberUtil.parseInt(args[0].trim().split("-")[0], -1);
            endIndex = NumberUtil.parseInt(args[0].trim().split("-")[1], -1);
        } catch (ArrayIndexOutOfBoundsException e) {
            return sendErrorMessage(context, context.i18n("mustBePositiveNumber"));
        }
        if (startIndex < 1 || endIndex < 1) {
            return sendErrorMessage(context, context.i18n("mustBePositiveNumber"));
        }

        GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild());

        if (musicManager.getScheduler().getQueue().isEmpty()) {
            return sendErrorMessage(context, context.i18n("nothingToRemove", generateCommandPrefix(context.getMessage())));
        }

        if (startIndex > musicManager.getScheduler().getQueue().size()
            || endIndex > musicManager.getScheduler().getQueue().size()) {
            return sendErrorMessage(context, context.i18n("tooHighNumberGiven"),
                NumberUtil.formatNicely(musicManager.getScheduler().getQueue().size())
            );
        }

        if (startIndex > endIndex) {
            return sendErrorMessage(context, context.i18n("invalidRange"));
        }

        Iterator<AudioTrackContainer> iterator = musicManager.getScheduler().getQueue().iterator();

        for (int counter = 0; counter < endIndex; counter++) {
            iterator.next();

            if (counter >= startIndex - 1) {
                iterator.remove();
            }
        }
        context.makeInfo(context.i18n("success"))
            .set("start", startIndex)
            .set("end", endIndex)
            .queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES, null, RestActionUtil.ignore));

        return true;
    }
}

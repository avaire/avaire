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
import java.util.*;
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
        return "Removes a song or multiple songs from the music queue.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <song id>` Removes the song with the given ID from the queue.",
            "`:command <start song id> - <end song id>` Removes the songs with the given range of IDs, inclusive, from the queue"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command 3` Removes song 3 from the queue",
            "`:command 3-6` Removes songs 3-6 from the queue"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("removesong", "songremove", "removesongs");
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

        GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild());

        if (musicManager.getScheduler().getQueue().isEmpty()) {
            return sendErrorMessage(context, context.i18n("nothingToRemove", generateCommandPrefix(context.getMessage())));
        }

        if (args[0].indexOf('-') > 0) {
            try {
                int beforeHyphen = NumberUtil.parseInt(args[0].trim().split("-")[0], -1);
                int afterHyphen = NumberUtil.parseInt(args[0].trim().split("-")[1], -1);
                if (beforeHyphen < 1 || afterHyphen < 1) {
                    return sendErrorMessage(context, context.i18n("mustBePositiveNumber"));
                }

                if (beforeHyphen > afterHyphen) {
                    return sendErrorMessage(context, context.i18n("invalidRange"));
                }

                if (beforeHyphen > musicManager.getScheduler().getQueue().size()
                    || afterHyphen > musicManager.getScheduler().getQueue().size()) {
                    return sendErrorMessage(context, context.i18n("tooHighNumberGiven"),
                        NumberUtil.formatNicely(musicManager.getScheduler().getQueue().size())
                    );
                }

                if (beforeHyphen == afterHyphen) {
                    return removeSingle(context, musicManager, beforeHyphen);
                }

                return removeMultiple(context, musicManager, beforeHyphen, afterHyphen);
            } catch (ArrayIndexOutOfBoundsException e) {
                return sendErrorMessage(context, context.i18n("mustBePositiveNumber"));
            }
        }

        int removeIndex = NumberUtil.parseInt(args[0], -1);
        if (removeIndex < 1) {
            return sendErrorMessage(context, context.i18n("mustBePositiveNumber"));
        }

        if (removeIndex > musicManager.getScheduler().getQueue().size()) {
            return sendErrorMessage(context, context.i18n("tooHighNumberGiven"),
                NumberUtil.formatNicely(musicManager.getScheduler().getQueue().size())
            );
        }

        return removeSingle(context, musicManager, removeIndex);
    }

    private boolean removeSingle(CommandMessage context, GuildMusicManager musicManager, int index) {
        Iterator<AudioTrackContainer> iterator = musicManager.getScheduler().getQueue().iterator();

        int counter = 0;
        while (iterator.hasNext()) {
            AudioTrackContainer next = iterator.next();

            if (++counter != index) {
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
            .set("index", index)
            .queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES, null, RestActionUtil.ignore));

        return false;
    }

    private boolean removeMultiple(CommandMessage context, GuildMusicManager musicManager, int startIndex, int endIndex) {
        Iterator<AudioTrackContainer> iterator = musicManager.getScheduler().getQueue().iterator();

        List<String> removedTracks = new ArrayList<>();
        for (int counter = 0; counter < endIndex; counter++) {
            AudioTrackContainer trackContainer = iterator.next();

            if (counter >= startIndex - 1) {
                AudioTrackInfo track = trackContainer.getAudioTrack().getInfo();
                removedTracks.add(String.format("**%d** [%s](%s)",
                    counter + 1, track.title, track.uri
                ));
                iterator.remove();
            }
        }

        context.makeInfo(context.i18n("successMultiple"))
            .set("start", startIndex)
            .set("end", endIndex)
            .set("tracks", formatRemovedTracks(context, removedTracks))
            .queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES, null, RestActionUtil.ignore));

        return true;
    }

    private String formatRemovedTracks(CommandMessage context, List<String> removedTracks) {
        if (removedTracks.size() <= 10) {
            return String.join("\n", removedTracks);
        }

        return String.join("\n", Arrays.copyOfRange(removedTracks.toArray(
            new String[removedTracks.size()]
        ), 0, 10)) + context.i18n("successMultipleNote", removedTracks.size() - 10);
    }
}

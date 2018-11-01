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
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RestActionUtil;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SeekCommand extends Command {

    public SeekCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Seek Command";
    }

    @Override
    public String getDescription() {
        return "Jumps to the given time code in the track that is currently playing.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <time>` - Jumps to the given time code.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command 2:24` - Jumps to 2 minutes 24 seconds of the song.");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("seek", "goto");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:normal",
            "throttle:guild,2,4",
            "musicChannel"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MUSIC_TRACK_MODIFIER);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild());

        if (!musicManager.isReady() || musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context, context.i18n("error", generateCommandPrefix(context.getMessage())));
        }

        if (!musicManager.canPreformSpecialAction(this, context, "seek a song")) {
            return false;
        }

        if (musicManager.getPlayer().getPlayingTrack().getInfo().isStream) {
            return sendErrorMessage(context, context.i18n("seekingLive"));
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "number");
        }

        try {
            long time = NumberUtil.parseTimeString(args[0]);

            if (time > musicManager.getPlayer().getPlayingTrack().getDuration()) {
                return sendErrorMessage(context, context.i18n("seekingTooFar", args[0], generateCommandPrefix(context.getMessage())));
            }

            musicManager.getPlayer().seekTo(time);

            context.makeSuccess(context.i18n("seekTo"))
                .set("title", musicManager.getPlayer().getPlayingTrack().getInfo().title)
                .set("time", NumberUtil.formatTime(time))
                .queue(message -> message.delete().queueAfter(3, TimeUnit.MINUTES, null, RestActionUtil.ignore));
        } catch (IllegalStateException ex) {
            return sendErrorMessage(context, context.i18n("invalidTimeGiven"));
        }
        return true;
    }
}

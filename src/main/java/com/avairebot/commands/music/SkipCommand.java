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
import com.avairebot.contracts.middleware.DJCheckMessage;
import com.avairebot.contracts.middleware.ThrottleMessage;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@DJCheckMessage(message = "\nYou can use the `:prefixvoteskip` command to vote to skip the song.", overwrite = false)
@ThrottleMessage(message = "Too many `:command` attempts. Please try again in **:time** seconds, or use the `:prefixclearqueue` command to remove all songs from the queue.")
public class SkipCommand extends Command {

    public SkipCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Skip Music Command";
    }

    @Override
    public String getDescription() {
        return "Skips to the next song in the music queue.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Skips to the next song in the queue");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(VoteSkipCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("skip");
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
        return Collections.singletonList(CommandGroups.MUSIC_SKIP);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild());

        if (!musicManager.isReady() || musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context,
                context.i18n("error"),
                generateCommandPrefix(context.getMessage())
            );
        }

        if (!musicManager.canPreformSpecialAction(this, context, "skip song")) {
            return false;
        }

        if (!musicManager.getScheduler().getQueue().isEmpty()) {
            AudioHandler.getDefaultAudioHandler().skipTrack(context);
            return true;
        }

        musicManager.getPlayer().stopTrack();
        musicManager.getScheduler().handleEndOfQueue(context, true);

        return true;
    }
}

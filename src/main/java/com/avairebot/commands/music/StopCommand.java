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
import com.avairebot.audio.LavalinkManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.scheduler.tasks.MusicActivityTask;
import com.avairebot.utilities.RestActionUtil;
import lavalink.client.io.jda.JdaLink;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StopCommand extends Command {

    public StopCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Stop Command";
    }

    @Override
    public String getDescription() {
        return "Stops the song currently playing, clears the music queue and disconnects from the voice channel the music was playing in.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Stops the music if anything is playing.");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("stop");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:normal",
            "throttle:guild,1,5",
            "musicChannel"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MUSIC_QUEUE);
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild());

        if (!musicManager.isReady() || musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context, context.i18n("error"));
        }

        if (!musicManager.canPreformSpecialAction(this, context, "stop music")) {
            return false;
        }

        String guildId = context.getGuild().getId();
        int size = musicManager.getScheduler().getQueue().size();

        musicManager.setRepeatState(GuildMusicManager.RepeatState.LOOPOFF);
        musicManager.getPlayer().stopTrack();
        musicManager.getScheduler().getQueue().clear();

        MusicActivityTask.missingListener.remove(guildId);
        MusicActivityTask.playerPaused.remove(guildId);
        MusicActivityTask.emptyQueue.remove(guildId);

        if (LavalinkManager.LavalinkManagerHolder.lavalink.isEnabled()) {
            JdaLink link = LavalinkManager.LavalinkManagerHolder.lavalink.getLavalink()
                .getLink(context.getGuild());

            if (!LavalinkManager.LavalinkManagerHolder.lavalink.isLinkBeingDestroyed(link)) {
                link.destroy();
            }
        }

        musicManager.getScheduler().nextTrack(false);

        context.makeInfo(context.i18n("success"))
            .set("number", size)
            .queue(message -> message.delete().queueAfter(5, TimeUnit.MINUTES, null, RestActionUtil.ignore));

        return true;
    }
}

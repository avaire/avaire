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
import com.avairebot.audio.VoiceConnectStatus;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MoveHereCommand extends Command {

    public MoveHereCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Move Music Here Command";
    }

    @Override
    public String getDescription() {
        return "Moves the bot to your current voice channel.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Moves the bot to your voice channel");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("movehere", "moveh");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:normal",
            "throttle:guild,1,4",
            "musicChannel"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (context.getGuildTransformer() == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "music channels");
        }

        GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild());

        if (!musicManager.isReady() || musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context, context.i18n("error", generateCommandPrefix(context.getMessage())));
        }

        VoiceChannel channel = context.getMember().getVoiceState().getChannel();
        if (channel == null) {
            return sendErrorMessage(context, "errors.mustBeConnectedToVoice");
        }

        if (context.getGuildTransformer().getMusicChannelVoice() != null) {
            VoiceChannel voiceChannel = context.getGuild().getVoiceChannelById(context.getGuildTransformer().getMusicChannelVoice());
            if (voiceChannel != null) {
                return sendErrorMessage(context, context.i18n("hasMusicChannel"));
            }
        }

        VoiceConnectStatus voiceConnectStatus = AudioHandler.getDefaultAudioHandler().connectToVoiceChannel(context, true);

        if (!voiceConnectStatus.isSuccess()) {
            context.makeWarning(voiceConnectStatus.getErrorMessage()).queue();
            return false;
        }

        context.makeSuccess(context.i18n("nowPlayingIn"))
            .set("channelName", channel.getName())
            .queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES, null, RestActionUtil.ignore));

        return true;
    }
}

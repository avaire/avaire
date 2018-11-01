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
import com.avairebot.audio.LavalinkManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.GuildVoiceState;
import net.dv8tion.jda.core.entities.VoiceChannel;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VoteSkipCommand extends Command {

    public VoteSkipCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Vote Skip Command";
    }

    @Override
    public String getDescription() {
        return "Use this command to vote on the song currently playing to be skipped, if the vote wins with a majority vote the song will be skipped.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Vote to skip the current song.");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(SkipCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("voteskip", "vskip");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "throttle:user,1,4",
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
            return sendErrorMessage(context, context.i18n("error", generateCommandPrefix(context.getMessage())));
        }

        if (!musicManager.canPreformSpecialAction(this, context, "vote skip track")) {
            return false;
        }

        if (!canVoteSkip(context)) {
            return sendErrorMessage(context, "errors.mustBeConnectedToSameChannel", "vote skip");
        }

        boolean hasVotedBefore = true;
        if (!getSkipsFrom(musicManager).contains(context.getAuthor().getIdLong())) {
            getSkipsFrom(musicManager).add(context.getAuthor().getIdLong());
            hasVotedBefore = false;
        }

        int usersInVoiceLength = getAmountOfUsersConnectedToVoice(context);
        double votePercentage = getVotePercentage(usersInVoiceLength, getSkipsFrom(musicManager).size());

        if (votePercentage >= 50) {
            if (!musicManager.getScheduler().getQueue().isEmpty()) {
                AudioHandler.getDefaultAudioHandler().skipTrack(context);
                return true;
            }

            musicManager.getPlayer().stopTrack();
            musicManager.getScheduler().handleEndOfQueue(context, true);
            return true;
        }

        int neededVotes = getNeededVotes(usersInVoiceLength, getSkipsFrom(musicManager).size());

        context.makeWarning(hasVotedBefore ?
            context.i18n("alreadyVoted") :
            context.i18n("registered")
        ).set("votes", neededVotes).queue(message -> {
            message.delete().queueAfter(1, TimeUnit.MINUTES, null, RestActionUtil.ignore);
        });

        return true;
    }

    private List<Long> getSkipsFrom(GuildMusicManager manager) {
        AudioTrackContainer trackContainer = manager.getScheduler().getAudioTrackContainer();
        if (trackContainer == null) {
            return Collections.emptyList();
        }
        return trackContainer.getSkips();
    }

    private int getAmountOfUsersConnectedToVoice(CommandMessage context) {
        VoiceChannel connectedChannel = LavalinkManager.LavalinkManagerHolder.lavalink.getConnectedChannel(context.getGuild());
        if (connectedChannel == null) {
            return 0;
        }

        return connectedChannel.getMembers().stream()
            .filter(m -> !m.getUser().isBot()).collect(Collectors.toList()).size();
    }

    private double getVotePercentage(int usersInVoiceLength, int voteSkips) {
        return Math.ceil(((double) voteSkips / (double) usersInVoiceLength) * 100);
    }

    private int getNeededVotes(int usersInVoiceLength, int voteSkips) {
        for (int i = 1; i < (usersInVoiceLength + 1); i++) {
            if (getVotePercentage(usersInVoiceLength, voteSkips + i) >= 50) {
                return i;
            }
        }

        return (int) Math.ceil(usersInVoiceLength / 2);
    }

    private boolean canVoteSkip(CommandMessage message) {
        GuildVoiceState voiceState = message.getMember().getVoiceState();
        GuildVoiceState selfVoteState = message.getGuild().getSelfMember().getVoiceState();

        return voiceState.getChannel() != null
            && selfVoteState.getChannel() != null
            && voiceState.getChannel().getId().equals(selfVoteState.getChannel().getId());
    }
}

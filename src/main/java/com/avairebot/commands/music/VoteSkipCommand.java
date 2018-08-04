package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.audio.LavalinkManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.GuildVoiceState;
import net.dv8tion.jda.core.entities.VoiceChannel;

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

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context, context.i18n("error", generateCommandPrefix(context.getMessage())));
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
        return manager.getScheduler().getAudioTrackContainer().getSkips();
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

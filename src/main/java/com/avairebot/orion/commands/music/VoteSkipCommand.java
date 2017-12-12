package com.avairebot.orion.commands.music;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.GuildMusicManager;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.GuildVoiceState;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VoteSkipCommand extends Command {

    public VoteSkipCommand(Orion orion) {
        super(orion, false);
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
        return null;
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("voteskip", "vskip");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,1,4");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(message, "Nothing to skip, request music first with `!play`");
        }

        if (!canVoteSkip(message)) {
            return sendErrorMessage(message, "You must be connected to the same voice channel I am in to vote skip!");
        }

        boolean hasVotedBefore = true;
        if (!getSkipsFrom(musicManager).contains(message.getAuthor().getIdLong())) {
            getSkipsFrom(musicManager).add(message.getAuthor().getIdLong());
            hasVotedBefore = false;
        }

        int usersInVoiceLength = getAmountOfUsersConnectedToVoice(message);
        double votePercentage = getVotePercentage(usersInVoiceLength, getSkipsFrom(musicManager).size());

        if (votePercentage >= 50) {
            if (!musicManager.getScheduler().getQueue().isEmpty()) {
                AudioHandler.skipTrack(message);
                return true;
            }

            musicManager.getPlayer().stopTrack();
            musicManager.getScheduler().handleEndOfQueue(message);
            return true;
        }

        int neededVotes = getNeededVotes(usersInVoiceLength, getSkipsFrom(musicManager).size());

        MessageFactory.makeWarning(message, hasVotedBefore ?
            "You can only vote skip once per song! `:votes` more votes needed to skip the song." :
            "Your vote has been registered! `:votes` more votes needed to skip the song."
        ).set("votes", neededVotes).queue();

        return true;
    }


    private List<Long> getSkipsFrom(GuildMusicManager manager) {
        return manager.getScheduler().getAudioTrackContainer().getSkips();
    }

    private int getAmountOfUsersConnectedToVoice(Message message) {
        return message.getGuild().getAudioManager().getConnectedChannel().getMembers().stream()
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

    private boolean canVoteSkip(Message message) {
        GuildVoiceState voiceState = message.getMember().getVoiceState();
        GuildVoiceState selfVoteState = message.getGuild().getSelfMember().getVoiceState();

        return voiceState.getChannel() != null && voiceState.getChannel().getId().equals(selfVoteState.getChannel().getId());
    }
}

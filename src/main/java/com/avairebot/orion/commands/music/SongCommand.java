package com.avairebot.orion.commands.music;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.AudioTrackContainer;
import com.avairebot.orion.audio.GuildMusicManager;
import com.avairebot.orion.audio.TrackScheduler;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import com.avairebot.orion.factories.MessageFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SongCommand extends AbstractCommand {

    public SongCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Music Song Command";
    }

    @Override
    public String getDescription() {
        return "Displays information about the song currently playing";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`!song` - Shows info about the song");
    }

    @Override
    public String getExampleUsage() {
        return "`!song`";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("song", "songs", "queue");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(message, "Nothing to display, request music first with `!play`");
        }

        MessageEmbed embed = MessageFactory.createEmbeddedBuilder()
                .setColor(MessageFactory.MessageType.SUCCESS.getColor())
                .setTitle(musicManager.getPlayer().isPaused() ? "Currently Paused" : "Currently Playing")
                .setDescription(buildTrackDescription(musicManager.getPlayer(), musicManager.getScheduler()))
                .addField("Songs in queue", buildSongsInQueue(musicManager.getScheduler()), false)
                .build();

        message.getChannel().sendMessage(embed).queue();
        return true;
    }

    private String buildTrackDescription(AudioPlayer player, TrackScheduler scheduler) {
        return String.format(
                "[%s](%s)\nPlaying at `%s` volume with `%s` left of the song - Requested by <@%s>",
                player.getPlayingTrack().getInfo().title,
                player.getPlayingTrack().getInfo().uri,
                player.getVolume() + "%",
                scheduler.getAudioTrackContainer().getFormattedTotalTimeLeft(),
                scheduler.getAudioTrackContainer().getRequester().getId()
        );
    }

    private String buildSongsInQueue(TrackScheduler scheduler) {
        if (scheduler.getQueue().isEmpty()) {
            return "There are no other songs in the queue right now";
        }

        int number = 1;
        String songs = "";

        Iterator<AudioTrackContainer> iterator = scheduler.getQueue().iterator();
        while (iterator.hasNext() && number <= 6) {
            AudioTrackContainer next = iterator.next();

            songs += String.format("**%s** [%s](%s)\n", number++, next.getAudioTrack().getInfo().title, next.getAudioTrack().getInfo().uri);
        }

        if (scheduler.getQueue().size() > 6) {
            songs += String.format("_And **%s** more song%s!_", scheduler.getQueue().size() - 6, scheduler.getQueue().size() == 7 ? "" : 's');
        }

        return songs;
    }
}

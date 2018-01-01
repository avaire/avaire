package com.avairebot.orion.commands.music;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.AudioSession;
import com.avairebot.orion.audio.TrackResponse;
import com.avairebot.orion.audio.VoiceConnectStatus;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.metrics.Metrics;
import com.avairebot.orion.utilities.NumberUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class PlayCommand extends Command {

    public PlayCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Play Music Command";
    }

    @Override
    public String getDescription() {
        return "Plays the provided song for you, if just the song title is given the bot will search YouTube for your song.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <song>` - Plays the given song");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("play", "request");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:guild,2,5");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "Missing music `query`, you must include a link to the song you want to listen to!");
        }

        boolean shouldLeaveMessage = false;
        if (args[args.length - 1].equals("---leave-message")) {
            shouldLeaveMessage = true;
            args = Arrays.copyOfRange(args, 0, args.length - 1);
        }

        VoiceConnectStatus voiceConnectStatus = AudioHandler.connectToVoiceChannel(message);
        if (!voiceConnectStatus.isSuccess()) {
            MessageFactory.makeWarning(message, voiceConnectStatus.getErrorMessage()).queue();
            return false;
        }

        if (AudioHandler.hasAudioSession(message) && NumberUtil.isNumeric(args[0])) {
            int songIndex = NumberUtil.parseInt(args[0], 1) - 1;
            AudioSession session = AudioHandler.getAudioSession(message);

            int index = NumberUtil.getBetween(songIndex, 0, session.getSongs().getTracks().size() - 1);
            AudioTrack track = session.getSongs().getTracks().get(index);

            Metrics.tracksLoaded.inc();

            musicSuccess(message, false).accept(
                new TrackResponse(AudioHandler.getGuildAudioPlayer(message.getGuild()),
                    track,
                    track.getInfo().uri
                )
            );
            AudioHandler.play(message, AudioHandler.getGuildAudioPlayer(message.getGuild()), track);

            if (session.getMessage() != null) {
                session.getMessage().delete().queue();
            }

            AudioHandler.removeAudioSession(message);
            return false;
        }

        boolean finalShouldLeaveMessage = shouldLeaveMessage;

        AudioHandler.loadAndPlay(message, buildTrackRequestString(args)).handle(
            musicSuccess(message, finalShouldLeaveMessage),
            musicFailure(message),
            musicSession(message, args)
        );

        return true;
    }

    private void sendPlaylistResponse(Message message, TrackResponse response) {
        AudioPlaylist playlist = (AudioPlaylist) response.getAudioItem();

        MessageFactory.makeSuccess(message,
            ":user has added :songs songs from the [:title](:url) playlist to the queue. There are `:queueSize` song(s) ahead of it in the queue."
        )
            .set("songs", playlist.getTracks().size())
            .set("title", playlist.getName())
            .set("url", response.getTrackUrl())
            .set("queueSize", AudioHandler.getQueueSize(response.getMusicManager()))
            .queue();
    }

    private void sendTrackResponse(Message message, TrackResponse response) {
        AudioTrack track = (AudioTrack) response.getAudioItem();

        MessageFactory.makeSuccess(message,
            ":user has added [:title](:url) to the queue. There are `:queueSize` song(s) ahead of it in the queue."
        )
            .set("title", track.getInfo().title)
            .set("url", track.getInfo().uri)
            .set("queueSize", AudioHandler.getQueueSize(response.getMusicManager()))
            .queue();
    }

    private String buildTrackRequestString(String[] args) {
        String string = String.join(" ", args);

        if (string.startsWith("scsearch:")) {
            return string;
        }

        try {
            new URL(string);

            return string;
        } catch (MalformedURLException ex) {
            return "ytsearch:" + string;
        }
    }

    private Consumer<TrackResponse> musicSuccess(final Message message, final boolean finalShouldLeaveMessage) {
        return (TrackResponse response) -> {
            if (!finalShouldLeaveMessage && message.getGuild().getSelfMember().hasPermission(message.getTextChannel(), Permission.MESSAGE_MANAGE)) {
                message.delete().reason("Song request, removing song to cleanup chat").queue();
            }

            if (response.getMusicManager().getPlayer().isPaused()) {
                response.getMusicManager().getPlayer().setPaused(false);
            }

            if (response.getMusicManager().getPlayer().getPlayingTrack() != null) {
                if (response.isPlaylist()) sendPlaylistResponse(message, response);
                else sendTrackResponse(message, response);
            }
        };
    }

    private Consumer<Throwable> musicFailure(final Message message) {
        return throwable -> MessageFactory.makeError(message, throwable.getMessage()).queue();
    }

    private Consumer<AudioSession> musicSession(final Message message, final String[] args) {
        return (AudioSession audioSession) -> {
            List<String> songs = new ArrayList<>();
            List<AudioTrack> tracks = audioSession.getSongs().getTracks();
            for (int i = 0; i < 9; i++) {
                if (tracks.size() < i) {
                    break;
                }

                AudioTrack track = tracks.get(i);

                songs.add(String.format("`%s` [%s](%s)",
                    (i + 1), track.getInfo().title, track.getInfo().uri
                ));
            }

            MessageFactory.makeSuccess(message, String.join("\n", songs))
                .setTitle("Results for " + String.join(" ", args))
                .setFooter("Chose a song with !play <number>")
                .queue(audioSession::setMessage);
        };
    }
}

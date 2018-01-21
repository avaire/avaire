package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.AudioSession;
import com.avairebot.audio.TrackResponse;
import com.avairebot.audio.VoiceConnectStatus;
import com.avairebot.commands.CommandContainer;
import com.avairebot.commands.CommandHandler;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.MessageFactory;
import com.avairebot.metrics.Metrics;
import com.avairebot.utilities.NumberUtil;
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

    public PlayCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Play Music Command";
    }

    @Override
    public String getDescription() {
        return "Plays the provided song for you, if just the song title is given the bot will search YouTube for your song and give you some suggestions, you can also use YouTube, SoundCloud, TwitchTV, Bandcamp, and Vimeo link, or raw sound file, mp3, flac, wav, webm, mp4, ogg, aac, m3u and pls formats.";
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
        return Arrays.asList(
            "has-dj-level:none",
            "throttle:guild,2,4"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command A cool song` - Finds songs with the name \"A cool song\".",
            "`:command https://www.youtube.com/watch?v=dQw4w9WgXcQ` - Plays the song off a link"
        );
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "Missing music `query`, you must include a link to the song you want to listen to, or at least give me a song title!");
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
            return loadSongFromSession(message, args);
        }

        boolean finalShouldLeaveMessage = shouldLeaveMessage;

        AudioHandler.loadAndPlay(message, buildTrackRequestString(args)).handle(
            musicSuccess(message, finalShouldLeaveMessage),
            musicFailure(message),
            musicSession(message, args)
        );

        return true;
    }

    public boolean loadSongFromSession(Message message, String[] args) {
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

    private void sendPlaylistResponse(Message message, TrackResponse response) {
        AudioPlaylist playlist = (AudioPlaylist) response.getAudioItem();

        MessageFactory.makeSuccess(message,
            ":user has added :songs songs from the [:title](:url) playlist to the queue. There are `:queueSize` song(s) ahead of it in the queue."
        )
            .set("songs", NumberUtil.formatNicely(playlist.getTracks().size()))
            .set("title", playlist.getName())
            .set("url", response.getTrackUrl())
            .set("queueSize", NumberUtil.formatNicely(
                AudioHandler.getQueueSize(response.getMusicManager())
            ))
            .queue();
    }

    private void sendTrackResponse(Message message, TrackResponse response) {
        AudioTrack track = (AudioTrack) response.getAudioItem();

        MessageFactory.makeSuccess(message,
            ":user has added [:title](:url) to the queue. There are `:queueSize` song(s) ahead of it in the queue."
        )
            .set("title", track.getInfo().title)
            .set("url", track.getInfo().uri)
            .set("queueSize", NumberUtil.formatNicely(
                AudioHandler.getQueueSize(response.getMusicManager())
            ))
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

            String command = generateCommandTrigger(message);
            if (args[0].startsWith("scsearch:")) {
                CommandContainer container = CommandHandler.getCommand(SoundcloudCommand.class);
                if (container != null) {
                    command = container.getCommand().generateCommandTrigger(message);
                }
            }

            MessageFactory.makeSuccess(message, String.join("\n", songs))
                .setTitle("Results for " + String.join(" ", args))
                .setFooter(String.format("Chose a song with %s <number>", command))
                .queue(audioSession::setMessage);
        };
    }
}

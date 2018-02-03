package com.avairebot.audio;

import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.NumberUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.player.IPlayer;
import lavalink.client.player.event.AudioEventAdapterWrapped;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapterWrapped {

    public final ExecutorService service = Executors.newCachedThreadPool();

    private final GuildMusicManager manager;
    private final IPlayer player;
    private final BlockingQueue<AudioTrackContainer> queue;

    private AudioTrackContainer audioTrackContainer;

    /**
     * @param manager The guild music manager.
     * @param player  The audio player this scheduler uses.
     */
    public TrackScheduler(GuildMusicManager manager, IPlayer player) {
        this.manager = manager;
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track     The track to play or add to queue.
     * @param requester The user who requested the audio track.
     */
    public void queue(AudioTrack track, User requester) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        AudioTrackContainer container = new AudioTrackContainer(track, requester);

        if (player.getPlayingTrack() != null) {
            queue.offer(container);
            return;
        }

        if (manager.getLastActiveMessage() != null) {
            player.playTrack(track);
            audioTrackContainer = container;
            sendNowPlaying(container);
        }
    }

    /**
     * Add the first track in the playlist to the queue or play right away if nothing is in the queue, then adds the rest of the tracks to the queue.
     *
     * @param playlist  The playlist of tracks to play or add to the queue.
     * @param requester The user who requested the audio track.
     */
    public void queue(AudioPlaylist playlist, User requester) {
        AudioTrack track = playlist.getTracks().get(0);
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        AudioTrackContainer container = new AudioTrackContainer(track, requester);

        if (player.getPlayingTrack() != null) {
            for (int i = 0; i < playlist.getTracks().size(); i++) {
                queue.offer(new AudioTrackContainer(playlist.getTracks().get(i), requester));
            }
            return;
        }

        if (manager.getLastActiveMessage() != null) {
            player.playTrack(track);
            audioTrackContainer = container;

            String message = "Now playing: [:title](:link)\n`:duration` - Requested by :requester";
            if (playlist.getName() != null) {
                message = "The **:playlistName** playlist has been added to the queue with `:playlistSize` tracks!\n" + message;
            }

            MessageFactory.makeSuccess(manager.getLastActiveMessage(), message)
                .set("title", container.getAudioTrack().getInfo().title)
                .set("link", container.getAudioTrack().getInfo().uri)
                .set("playlistSize", NumberUtil.formatNicely(playlist.getTracks().size()))
                .set("playlistName", playlist.getName())
                .set("duration", container.getFormattedDuration())
                .set("requester", container.getRequester().getAsMention())
                .queue();
        }


        for (int i = 1; i < playlist.getTracks().size(); i++) {
            queue.offer(new AudioTrackContainer(playlist.getTracks().get(i), requester));
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        AudioTrackContainer container = queue.poll();

        if (container == null) {
            player.playTrack(null);
            if (manager.getLastActiveMessage() == null)
                return;

            service.submit(this::handleEndOfQueueWithLastActiveMessage);
            return;
        }

        audioTrackContainer = container;
        player.playTrack(container.getAudioTrack());
        if (manager.getLastActiveMessage() != null) {
            sendNowPlaying(container);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (manager.isRepeatQueue()) {
                queue.offer(new AudioTrackContainer(track.makeClone(), getAudioTrackContainer().getRequester()));
            }

            nextTrack();
            return;
        }

        if (endReason.equals(AudioTrackEndReason.FINISHED) && queue.isEmpty()) {
            if (manager.getLastActiveMessage() != null) {
                service.submit(this::handleEndOfQueueWithLastActiveMessage);
            }
        }
    }

    public BlockingQueue<AudioTrackContainer> getQueue() {
        return queue;
    }

    public AudioTrackContainer getAudioTrackContainer() {
        return audioTrackContainer;
    }

    private void sendNowPlaying(AudioTrackContainer container) {
        MessageFactory.makeSuccess(manager.getLastActiveMessage(), "Now playing: [:title](:link)\n`:duration` - Requested by :requester")
            .set("title", container.getAudioTrack().getInfo().title)
            .set("link", container.getAudioTrack().getInfo().uri)
            .set("duration", container.getFormattedDuration())
            .set("requester", container.getRequester().getAsMention())
            .queue();
    }

    public void handleEndOfQueue(Message message) {
        MessageFactory.makeSuccess(message, "Queue has ended, leaving voice.").queue();

        LavalinkManager.LavalinkManagerHolder.LAVALINK.closeConnection(message.getGuild());

        AudioHandler.MUSIC_MANAGER.remove(
            message.getGuild().getIdLong()
        );
    }

    public void handleEndOfQueueWithLastActiveMessage() {
        handleEndOfQueue(manager.getLastActiveMessage());
    }
}

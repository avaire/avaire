package com.avairebot.audio;

import com.avairebot.commands.CommandMessage;
import com.avairebot.database.transformers.PlaylistTransformer;
import com.avairebot.utilities.NumberUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.AudioEventAdapterWrapped;
import net.dv8tion.jda.core.entities.User;

import java.util.List;
import java.util.concurrent.*;

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
     * Adds the list of tracks to the queue, if the player is not playing a song the first track in the list will be played.
     *
     * @param tracks    The list of tracks to add to the queue.
     * @param requester The user who requested the audio tracks.
     */
    public void queue(PlaylistTransformer playlist, List<AudioTrack> tracks, User requester) {
        if (tracks.isEmpty()) {
            return;
        }

        int size = tracks.size();
        AudioTrackContainer container = new AudioTrackContainer(tracks.get(0), requester);

        String message = manager.getLastActiveMessage().i18nRaw("music.PlaylistCommand.loadedPlaylist");

        if (player.getPlayingTrack() == null) {
            message = message + "\n" + manager.getLastActiveMessage().i18nRaw("music.internal.nowPlayingSong");

            tracks.remove(0);
            player.playTrack(container.getAudioTrack());
            audioTrackContainer = container;
        }

        for (AudioTrack track : tracks) {
            queue.offer(new AudioTrackContainer(track, requester));
        }

        manager.getLastActiveMessage().makeSuccess(message)
            .set("title", container.getAudioTrack().getInfo().title)
            .set("link", container.getAudioTrack().getInfo().uri)
            .set("size", NumberUtil.formatNicely(size))
            .set("name", playlist.getName())
            .set("amount", size)
            .set("duration", container.getFormattedDuration())
            .set("requester", container.getRequester().getAsMention())
            .queue();
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

            String message = manager.getLastActiveMessage().i18nRaw("music.internal.nowPlayingSong");
            if (playlist.getName() != null) {
                message = manager.getLastActiveMessage().i18nRaw("music.internal.nowPlayingPlaylist")
                    + "\n" + message;
            }

            manager.getLastActiveMessage().makeSuccess(message)
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
     * Start the next track, stopping the current one if it is playing, if it's
     * the end of the queue the "End of the queue" message will be sent.
     */
    public void nextTrack() {
        nextTrack(true);
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack(boolean sendEndOfQueue) {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        AudioTrackContainer container = queue.poll();

        if (container == null) {
            if (manager.getLastActiveMessage() == null)
                return;

            service.submit(() -> handleEndOfQueueWithLastActiveMessage(sendEndOfQueue));
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
                service.submit(() -> handleEndOfQueueWithLastActiveMessage(true));
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
        manager.getLastActiveMessage().makeSuccess(
            manager.getLastActiveMessage().i18nRaw("music.internal.nowPlaying")
        )
            .set("title", container.getAudioTrack().getInfo().title)
            .set("link", container.getAudioTrack().getInfo().uri)
            .set("duration", container.getFormattedDuration())
            .set("requester", container.getRequester().getAsMention())
            .queue();
    }

    public void handleEndOfQueue(CommandMessage context, boolean sendEndOfQueue) {
        if (sendEndOfQueue && AudioHandler.MUSIC_MANAGER.containsKey(context.getGuild().getIdLong())) {
            context.makeSuccess(context.i18nRaw("music.internal.queueHasEnded"))
                .queue(queueMessage -> {
                    queueMessage.delete().queueAfter(45, TimeUnit.SECONDS);
                });
        }

        LavalinkManager.LavalinkManagerHolder.LAVALINK.closeConnection(context.getGuild());

        if (LavalinkManager.LavalinkManagerHolder.LAVALINK.isEnabled()) {
            GuildMusicManager manager = AudioHandler.MUSIC_MANAGER.get(context.getGuild().getIdLong());
            manager.getPlayer().removeListener(this);

            if (manager.getPlayer() instanceof LavalinkPlayer) {
                ((LavalinkPlayer) manager.getPlayer()).getLink().destroy();
            }
        }

        AudioHandler.MUSIC_MANAGER.remove(
            context.getGuild().getIdLong()
        );
    }

    public void handleEndOfQueueWithLastActiveMessage(boolean sendEndOfQueue) {
        handleEndOfQueue(manager.getLastActiveMessage(), sendEndOfQueue);
    }
}

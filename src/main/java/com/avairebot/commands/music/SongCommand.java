package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.AudioTrackContainer;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.audio.TrackScheduler;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.language.I18n;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RestActionUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.player.IPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SongCommand extends Command {

    public SongCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Music Song Command";
    }

    @Override
    public String getDescription() {
        return "Returns the song that is playing right now and some attached information. This includes who requested it, how much of the song is left and the volume the song is playing at plus the rest of the songs currently in queue.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Shows info about the song currently playing and the queue.",
            "`:command [page]` - Shows the songs in the given page in the queue."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("song", "songs", "queue");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:none",
            "throttle:channel,2,4",
            "musicChannel"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context, context.i18n("error", generateCommandPrefix(context.getMessage())));
        }

        if (args.length > 0 && NumberUtil.isNumeric(args[0])) {
            if (musicManager.getScheduler().getQueue().isEmpty()) {
                return sendSongWithSixSongs(context, musicManager);
            }

            SimplePaginator paginator = new SimplePaginator(
                musicManager.getScheduler().getQueue().iterator(), 10, NumberUtil.parseInt(args[0])
            );

            List<String> messages = new ArrayList<>();
            paginator.forEach((index, key, val) -> {
                AudioTrackContainer track = (AudioTrackContainer) val;

                messages.add(context.i18n("formats.line",
                    NumberUtil.parseInt(key.toString()) + 1,
                    track.getAudioTrack().getInfo().title,
                    track.getAudioTrack().getInfo().uri
                ));
            });

            context.makeSuccess(String.format("%s\n\n%s",
                String.join("\n", messages),
                paginator.generateFooter(generateCommandTrigger(context.getMessage()))
            )).setTitle(context.i18n("songsInQueue"))
                .queue(message -> message.delete().queueAfter(3, TimeUnit.MINUTES, null, RestActionUtil.ignore));

            return true;
        }

        return sendSongWithSixSongs(context, musicManager);
    }

    private boolean sendSongWithSixSongs(CommandMessage context, GuildMusicManager musicManager) {
        PlaceholderMessage queueMessage = context.makeSuccess(
            buildTrackDescription(context, musicManager.getPlayer(), musicManager.getScheduler())
        )
            .setTitle(musicManager.getPlayer().isPaused() ? context.i18n("paused") : context.i18n("playing"))
            .addField(
                context.i18n("songsInQueue") + " - " + buildQueueLength(musicManager),
                buildSongsInQueue(context, musicManager.getScheduler()),
                false
            );

        if (!musicManager.getScheduler().getQueue().isEmpty()) {
            queueMessage.setFooter(context.i18n("moreSongs", generateCommandTrigger(context.getMessage())));
        }

        queueMessage.queue(message -> message.delete().queueAfter(3, TimeUnit.MINUTES, null, RestActionUtil.ignore));

        return true;
    }

    private String buildTrackDescription(CommandMessage context, IPlayer player, TrackScheduler scheduler) {
        String message = context.i18n("formats.song");

        if (player.getPlayingTrack().getInfo().isStream) {
            message = context.i18n("formats.stream");
        }

        String songTitle = player.getPlayingTrack().getInfo().title;
        if (songTitle == null || songTitle.equalsIgnoreCase("Unknown Title")) {
            songTitle = player.getPlayingTrack().getInfo().uri;
        }

        return I18n.format(message,
            songTitle,
            player.getPlayingTrack().getInfo().uri,
            player.getVolume() + "%",
            scheduler.getAudioTrackContainer().getFormattedTotalTimeLeft(player),
            scheduler.getAudioTrackContainer().getRequester().getId()
        );
    }

    private String buildSongsInQueue(CommandMessage context, TrackScheduler scheduler) {
        if (scheduler.getQueue().isEmpty()) {
            return context.i18n("noSongs");
        }

        int number = 1;
        String songs = "";

        Iterator<AudioTrackContainer> iterator = scheduler.getQueue().iterator();
        while (iterator.hasNext() && number <= 6) {
            AudioTrackContainer next = iterator.next();

            songs += context.i18n("formats.line",
                number++,
                next.getAudioTrack().getInfo().title,
                next.getAudioTrack().getInfo().uri
            ) + "\n";
        }

        if (scheduler.getQueue().size() > 6) {
            songs += context.i18n("andXMoreSongs",
                NumberUtil.formatNicely(scheduler.getQueue().size() - 6),
                scheduler.getQueue().size() == 7 ? "" : 's'
            );
        }

        return songs;
    }

    private String buildQueueLength(GuildMusicManager manager) {
        long length = 0L;
        for (AudioTrackContainer container : manager.getScheduler().getQueue()) {
            if (container.getAudioTrack().getInfo().isStream) {
                continue;
            }
            length += container.getAudioTrack().getDuration() / 1000L;
        }

        AudioTrack playingTrack = manager.getPlayer().getPlayingTrack();
        if (playingTrack != null && !playingTrack.getInfo().isStream) {
            length += (playingTrack.getDuration() - playingTrack.getPosition()) / 1000L;
        }

        int seconds = (int) (length % 60L);
        int minutes = (int) ((length % 3600L) / 60L);
        int hours = (int) (length / 3600L);

        if (hours != 0) {
            return hours + " hours, " + minutes + " minutes, and " + seconds + " seconds";
        } else if (minutes != 0) {
            return minutes + " minutes, and " + seconds + " seconds";
        }
        return seconds + " seconds";
    }
}

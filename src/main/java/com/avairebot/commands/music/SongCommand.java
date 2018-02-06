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
import com.avairebot.utilities.NumberUtil;
import lavalink.client.player.IPlayer;

import java.util.*;
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
        return "Displays information about the song currently playing";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Shows info about the song");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("song", "songs", "queue");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "has-dj-level:none",
            "throttle:channel,2,4"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(context.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context,
                "Nothing to display, request music first with `%splay`",
                generateCommandPrefix(context.getMessage())
            );
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

                messages.add(String.format("**%s** [%s](%s)",
                    NumberUtil.parseInt(key.toString()) + 1,
                    track.getAudioTrack().getInfo().title,
                    track.getAudioTrack().getInfo().uri
                ));
            });

            context.makeSuccess(String.format("%s\n\n%s",
                String.join("\n", messages),
                paginator.generateFooter(generateCommandTrigger(context.getMessage()))
            )).setTitle("Songs in Queue")
                .queue(message -> message.delete().queueAfter(3, TimeUnit.MINUTES));

            return true;
        }

        return sendSongWithSixSongs(context, musicManager);
    }

    private boolean sendSongWithSixSongs(CommandMessage context, GuildMusicManager musicManager) {
        PlaceholderMessage queueMessage = context.makeSuccess(
            buildTrackDescription(musicManager.getPlayer(), musicManager.getScheduler())
        )
            .setTitle(musicManager.getPlayer().isPaused() ? "Currently Paused" : "Currently Playing")
            .addField("Songs in queue", buildSongsInQueue(musicManager.getScheduler()), false);

        if (!musicManager.getScheduler().getQueue().isEmpty()) {
            queueMessage.setFooter(String.format("You can see more songs by using %s <page>",
                generateCommandTrigger(context.getMessage())
            ));
        }

        queueMessage.queue(message -> message.delete().queueAfter(3, TimeUnit.MINUTES));

        return true;
    }

    private String buildTrackDescription(IPlayer player, TrackScheduler scheduler) {
        String message = "[%s](%s)\nPlaying at `%s` volume with `%s` left of the song - Requested by <@%s>";

        if (player.getPlayingTrack().getInfo().isStream) {
            message = "[%s](%s)\nPlaying at `%s` volume, the stream is `%s` - Requested by <@%s>";
        }

        return String.format(message,
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
            songs += String.format("_And **%s** more song%s!_",
                NumberUtil.formatNicely(scheduler.getQueue().size() - 6),
                scheduler.getQueue().size() == 7 ? "" : 's'
            );
        }

        return songs;
    }
}

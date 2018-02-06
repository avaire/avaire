package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.AudioTrackContainer;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.NumberUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RemoveSongFromQueueCommand extends Command {

    public RemoveSongFromQueueCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Remove Song From Queue";
    }

    @Override
    public String getDescription() {
        return "Removes a song from the music queue.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <song id>` Removes the song with the given ID from the queue."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("removesong", "songremove");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "has-dj-level:normal",
            "throttle:user,2,4"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "Missing argument `song id`, you must include the ID of the song you want to remove from the queue.");
        }

        int removeIndex = NumberUtil.parseInt(args[0], -1);
        if (removeIndex < 1) {
            return sendErrorMessage(context, "The `song id` must be a valid positive number.");
        }

        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(context.getGuild());

        if (musicManager.getScheduler().getQueue().isEmpty()) {
            return sendErrorMessage(context,
                "Nothing to remove, request music first with `%splay`",
                generateCommandPrefix(context.getMessage())
            );
        }

        if (removeIndex > musicManager.getScheduler().getQueue().size()) {
            return sendErrorMessage(context, "There are only `%s` songs in the queue, try lowering your number a bit.",
                NumberUtil.formatNicely(musicManager.getScheduler().getQueue().size())
            );
        }

        Iterator<AudioTrackContainer> iterator = musicManager.getScheduler().getQueue().iterator();

        int counter = 0;
        while (iterator.hasNext()) {
            AudioTrackContainer next = iterator.next();

            if (++counter != removeIndex) {
                continue;
            }

            AudioTrackInfo track = next.getAudioTrack().getInfo();
            context.makeInfo(":song has been successfully removed from the music queue.")
                .set("song", String.format("[%s](%s)",
                    track.title, track.uri
                ))
                .queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES));

            iterator.remove();
            return true;
        }

        context.makeError("Something went wrong, failed to remove song at index `:index`")
            .set("index", removeIndex)
            .queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES));

        return false;
    }
}

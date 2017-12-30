package com.avairebot.orion.commands.music;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.AudioTrackContainer;
import com.avairebot.orion.audio.GuildMusicManager;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.NumberUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RemoveSongFromQueueCommand extends Command {

    public RemoveSongFromQueueCommand(Orion orion) {
        super(orion, false);
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
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("removesong", "songremove");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "has-role:DJ",
            "throttle:user,2,4"
        );
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "Missing argument `song id`, you must include the ID of the song you want to remove from the queue.");
        }

        int removeIndex = NumberUtil.parseInt(args[0], -1);
        if (removeIndex < 1) {
            return sendErrorMessage(message, "The `song id` must be a valid positive number.");
        }

        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (musicManager.getScheduler().getQueue().isEmpty()) {
            return sendErrorMessage(message, "Nothing to remove, request music first with `!play`");
        }

        if (removeIndex > musicManager.getScheduler().getQueue().size()) {
            return sendErrorMessage(message, "There are only `%s` songs in the queue, try lowering your number a bit.",
                "" + musicManager.getScheduler().getQueue().size()
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
            MessageFactory.makeInfo(message, ":song has been successfully removed from the music queue.")
                .set("song", String.format("[%s](%s)",
                    track.title, track.uri
                ))
                .queue();

            iterator.remove();
            return true;
        }

        MessageFactory.makeError(message, "Something went wrong, failed to remove song at index `:index`")
            .set("index", removeIndex)
            .queue();

        return false;
    }
}

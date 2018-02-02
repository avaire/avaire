package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.AudioTrackContainer;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.NumberUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShuffleCommand extends Command {

    public ShuffleCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Shuffle Command";
    }

    @Override
    public String getDescription() {
        return "Shuffles the music queue, mixing the songs up in random order.";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("shuffle");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "has-dj-level:normal",
            "throttle:guild,2,4"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(context.getGuild());

        if (musicManager.getScheduler().getQueue().isEmpty()) {
            return sendErrorMessage(context, "The music queue is already empty, you can request something with \n`%splay <song title | url>`",
                generateCommandPrefix(context.getMessage())
            );
        }

        List<AudioTrackContainer> queue = new ArrayList<>();
        musicManager.getScheduler().getQueue().drainTo(queue);

        Collections.shuffle(queue);
        musicManager.getScheduler().getQueue().addAll(queue);

        context.makeSuccess("**:amount** songs has been shuffled in the music queue.")
            .set("amount", NumberUtil.formatNicely(queue.size()))
            .queue();

        return true;
    }
}

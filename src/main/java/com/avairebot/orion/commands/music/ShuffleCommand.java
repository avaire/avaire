package com.avairebot.orion.commands.music;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.AudioTrackContainer;
import com.avairebot.orion.audio.GuildMusicManager;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShuffleCommand extends Command {

    public ShuffleCommand(Orion orion) {
        super(orion);
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
    public List<String> getUsageInstructions() {
        return null;
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("shuffle");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "has-role:DJ",
            "throttle:guild,2,4"
        );
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (musicManager.getScheduler().getQueue().isEmpty()) {
            return sendErrorMessage(message, "The music queue is already empty, you can request something with \n`%splay <song title | url>`",
                generateCommandPrefix(message)
            );
        }

        List<AudioTrackContainer> queue = new ArrayList<>();
        musicManager.getScheduler().getQueue().drainTo(queue);

        Collections.shuffle(queue);
        musicManager.getScheduler().getQueue().addAll(queue);

        MessageFactory.makeSuccess(message, "**:amount** songs has been shuffled in the music queue.")
            .set("amount", queue.size())
            .queue();

        return true;
    }
}

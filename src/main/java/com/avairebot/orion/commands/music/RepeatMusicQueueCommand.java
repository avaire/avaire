package com.avairebot.orion.commands.music;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.GuildMusicManager;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.List;

public class RepeatMusicQueueCommand extends AbstractCommand {

    public RepeatMusicQueueCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Repeat Music Command";
    }

    @Override
    public String getDescription() {
        return "Repeats all the songs in the music queue.";
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
        return Arrays.asList("repeatsongs", "repeat", "loop");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList("has-role:DJ", "throttle:guild,2,4");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(message, "There is nothing to repeat, request music first with `!play`");
        }

        musicManager.setRepeatQueue(!musicManager.isRepeatQueue());
        
        MessageFactory.makeSuccess(message, "Music queue looping has been turned `%s`.",
                musicManager.isRepeatQueue() ? "ON" : "OFF"
        ).queue();

        return true;
    }
}

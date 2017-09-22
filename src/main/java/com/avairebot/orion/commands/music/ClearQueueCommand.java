package com.avairebot.orion.commands.music;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.GuildMusicManager;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClearQueueCommand extends AbstractCommand {

    public ClearQueueCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Clear Music Queue Command";
    }

    @Override
    public String getDescription() {
        return "Clears the music queue of all pending songs";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Clears the music queue");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("clearqueue", "cqueue", "flushqueue");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList("has-role:DJ", "throttle:user,1,5");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(message, "Nothing to clear, request music first with `!play`");
        }

        if (musicManager.getScheduler().getQueue().isEmpty()) {
            MessageFactory.makeWarning(message, "Nothing to clear, there are no songs pending in the queue right now.").queue();
            return false;
        }

        MessageFactory.makeSuccess(message, "I have removed **%s** songs from the queue, the queue is now empty!",
                musicManager.getScheduler().getQueue().size()
        ).queue();
        musicManager.getScheduler().getQueue().clear();

        return true;
    }
}

package com.avairebot.orion.commands.music;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.GuildMusicManager;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PauseCommand extends Command {

    public PauseCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Pause Music Command";
    }

    @Override
    public String getDescription() {
        return "Pauses the music currently playing";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Pauses the music");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("pause");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList("has-role:DJ", "throttle:guild,1,4");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(message, "Nothing to pause, request music first with `!play`");
        }

        if (musicManager.getPlayer().isPaused()) {
            MessageFactory.makeWarning(message, "The music is already paused, use `!resume` to resume the music.").queue();
            return true;
        }

        musicManager.getPlayer().setPaused(true);
        MessageFactory.makeSuccess(message, "The music has been `paused`").queue();

        return true;
    }
}

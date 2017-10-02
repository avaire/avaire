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

public class SkipCommand extends Command {

    public SkipCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Skip Music Command";
    }

    @Override
    public String getDescription() {
        return "Skips to the next song in the music queue.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(":command` - Skips to the next song in the queue");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("skip");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList("has-role:DJ", "throttle:guild,2,4");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(message, "Nothing to skip, request music first with `!play`");
        }

        if (!musicManager.getScheduler().getQueue().isEmpty()) {
            AudioHandler.skipTrack(message);
            return true;
        }

        MessageFactory.makeSuccess(message, "Queue has ended, leaving voice.").queue();

        musicManager.getPlayer().stopTrack();
        message.getGuild().getAudioManager().closeAudioConnection();

        return true;
    }
}

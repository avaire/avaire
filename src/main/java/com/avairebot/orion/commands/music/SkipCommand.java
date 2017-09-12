package com.avairebot.orion.commands.music;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.GuildMusicManager;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.List;

public class SkipCommand extends AbstractCommand {

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
        return Arrays.asList("`!skip` - Skips to the next song in the queue");
    }

    @Override
    public String getExampleUsage() {
        return "`!skip`";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("skip");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(message, "Nothing to skip, request music first with `!play`");
        }

        AudioHandler.skipTrack(message);
        return true;
    }
}

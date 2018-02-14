package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.middleware.ThrottleMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ThrottleMessage(message = "Too many `:command` attempts. Please try again in **:time** seconds, or use the `:prefixclearqueue` command to remove all songs from the queue.")
public class SkipCommand extends Command {

    public SkipCommand(AvaIre avaire) {
        super(avaire, false);
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
        return Collections.singletonList("`:command` - Skips to the next song in the queue");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("skip");
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

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context, "Nothing to skip, request music first with `!play`");
        }

        if (!musicManager.getScheduler().getQueue().isEmpty()) {
            AudioHandler.skipTrack(context.getMessage());
            return true;
        }

        musicManager.getPlayer().stopTrack();
        musicManager.getScheduler().handleEndOfQueue(context.getMessage(), true);

        return true;
    }
}

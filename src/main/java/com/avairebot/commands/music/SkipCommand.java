package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.middleware.DJCheckMessage;
import com.avairebot.contracts.middleware.ThrottleMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@DJCheckMessage(message = "\nYou can use the `:prefixvoteskip` command to vote to skip the song.", overwrite = false)
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
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(VoteSkipCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("skip");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:normal",
            "throttle:guild,2,4",
            "musicChannel"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context,
                context.i18n("error"),
                generateCommandPrefix(context.getMessage())
            );
        }

        if (!musicManager.getScheduler().getQueue().isEmpty()) {
            AudioHandler.getDefaultAudioHandler().skipTrack(context);
            return true;
        }

        musicManager.getPlayer().stopTrack();
        musicManager.getScheduler().handleEndOfQueue(context, true);

        return true;
    }
}

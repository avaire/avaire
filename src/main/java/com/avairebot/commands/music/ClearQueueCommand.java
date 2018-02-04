package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.NumberUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClearQueueCommand extends Command {

    public ClearQueueCommand(AvaIre avaire) {
        super(avaire, false);
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
    public List<String> getTriggers() {
        return Arrays.asList("clearqueue", "cqueue", "flushqueue");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList("has-dj-level:normal", "throttle:user,1,5");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(context.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context,
                "Nothing to clear, request music first with `%splay`",
                generateCommandPrefix(context.getMessage())
            );
        }

        if (musicManager.getScheduler().getQueue().isEmpty()) {
            context.makeWarning("Nothing to clear, there are no songs pending in the queue right now.").queue();
            return false;
        }

        context.makeSuccess("I have removed **:queueSize** songs from the queue, the queue is now empty!")
            .set("queueSize", NumberUtil.formatNicely(
                musicManager.getScheduler().getQueue().size()
            ))
            .queue();

        musicManager.getScheduler().getQueue().clear();

        return true;
    }
}

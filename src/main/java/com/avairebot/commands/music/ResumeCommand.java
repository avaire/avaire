package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ResumeCommand extends Command {

    public ResumeCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Resume Music Command";
    }

    @Override
    public String getDescription() {
        return "Resumes the music in the queue, starting the music back up if it was paused";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Resumes the music");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("resume");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "has-dj-level:normal",
            "throttle:guild,1,4"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(context.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context,
                "Nothing to resume, request music first with `%splay`",
                generateCommandPrefix(context.getMessage())
            );
        }

        if (!musicManager.getPlayer().isPaused()) {
            context.makeWarning("The music is already playing, use `:prefixpause` to pause the music first.")
                .set("prefix", generateCommandPrefix(context.getMessage()))
                .queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES));

            return true;
        }

        musicManager.getPlayer().setPaused(false);
        context.makeSuccess("The music has been `resumed`")
            .queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES));

        return true;
    }
}

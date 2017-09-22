package com.avairebot.orion.commands.music;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.GuildMusicManager;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.Collections;
import java.util.List;

public class ResumeCommand extends AbstractCommand {

    public ResumeCommand(Orion orion) {
        super(orion, false);
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
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("resume");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(message, "Nothing to resume, request music first with `!play`");
        }

        if (!musicManager.getPlayer().isPaused()) {
            MessageFactory.makeWarning(message, "The music is already playing, use `!pause` to pause the music first.").queue();
            return true;
        }

        musicManager.getPlayer().setPaused(false);
        MessageFactory.makeSuccess(message, "The music has been `resumed`").queue();

        return true;
    }
}

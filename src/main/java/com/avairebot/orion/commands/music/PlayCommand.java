package com.avairebot.orion.commands.music;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlayCommand extends AbstractCommand {

    public PlayCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Play Music Command";
    }

    @Override
    public String getDescription() {
        return "Plays the music that you request for you.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`!play <song>` - Plays the given song");
    }

    @Override
    public String getExampleUsage() {
        return "`!play rick and morty`";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("play", "queue", "request");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "Missing music `query`, you must include a link to the song you want to listen to!");
        }

        AudioHandler.loadAndPlay(message.getTextChannel(), String.join("", args));
        return true;
    }
}

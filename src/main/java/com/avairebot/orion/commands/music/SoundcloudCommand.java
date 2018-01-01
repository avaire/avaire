package com.avairebot.orion.commands.music;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.contracts.commands.Command;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SoundcloudCommand extends Command {

    public SoundcloudCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "SoundCloud Command";
    }

    @Override
    public String getDescription() {
        return "Plays the provided song for you, if just the song title is given the bot will search SoundCloud for your song.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <song>` - Plays the given song");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("soundcloud", "sc");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:guild,2,5");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        CommandContainer container = CommandHandler.getCommand(PlayCommand.class);
        if (container == null) {
            return sendErrorMessage(message, "The `Play Command` doesn't exist anymore, this command doesn't work without it.");
        }

        return container.getCommand().onCommand(message, new String[]{
            "scsearch:", String.join(" ", args)
        });
    }
}

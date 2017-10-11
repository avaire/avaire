package com.avairebot.orion.commands.music;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.GuildMusicManager;
import com.avairebot.orion.audio.VoiceConnectStatus;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MoveHereCommand extends Command {

    public MoveHereCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Move Music Here Command";
    }

    @Override
    public String getDescription() {
        return "Moves the bot to your current voice channel.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Moves the bot to your voice channel");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("movehere", "moveh");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList("has-role:DJ", "throttle:guild,1,4");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(message, "Not connected to voice, request music first with `!play`");
        }

        VoiceChannel channel = message.getMember().getVoiceState().getChannel();
        if (channel == null) {
            return sendErrorMessage(message, "You must be connected to a voice channel to use this command!");
        }

        VoiceConnectStatus voiceConnectStatus = AudioHandler.connectToVoiceChannel(message, true);

        if (!voiceConnectStatus.isSuccess()) {
            MessageFactory.makeWarning(message, voiceConnectStatus.getErrorMessage()).queue();
            return false;
        }

        MessageFactory.makeSuccess(message, "I am now streaming music in **%s**",
            channel.getName()
        ).queue();
        return true;

    }
}

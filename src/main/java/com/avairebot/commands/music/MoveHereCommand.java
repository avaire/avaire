package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.audio.VoiceConnectStatus;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MoveHereCommand extends Command {

    public MoveHereCommand(AvaIre avaire) {
        super(avaire, false);
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
    public List<String> getTriggers() {
        return Arrays.asList("movehere", "moveh");
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
                "Not connected to voice, request music first with `%splay`",
                generateCommandPrefix(context.getMessage())
            );
        }

        VoiceChannel channel = context.getMember().getVoiceState().getChannel();
        if (channel == null) {
            return sendErrorMessage(context, "You must be connected to a voice channel to use this command!");
        }

        VoiceConnectStatus voiceConnectStatus = AudioHandler.connectToVoiceChannel(context.getMessage(), true);

        if (!voiceConnectStatus.isSuccess()) {
            context.makeWarning(voiceConnectStatus.getErrorMessage()).queue();
            return false;
        }

        context.makeSuccess("I am now streaming music in **:channelName**")
            .set("channelName", channel.getName())
            .queue();
        return true;
    }
}

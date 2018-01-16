package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.DJGuildLevel;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VolumeCommand extends Command {

    public VolumeCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Music Volume Command";
    }

    @Override
    public String getDescription() {
        return "Sets the volume of the music currently playing";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Shows the current music volume without changing it",
            "`:command <volume>` - Sets the music volume to the given number"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command 80`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("volume", "vol");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,1,4");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(message, "Nothing is playing, request music first with `!play`");
        }

        int volume = musicManager.getPlayer().getVolume();

        if (args.length == 0) {
            MessageFactory.makeSuccess(message, "\uD83C\uDFB5 Music is playing at **:volume** volume\n:bar")
                .set("volume", volume)
                .set("bar", getVolumeString(volume, 21))
                .queue();
            return true;
        }

        if (!AudioHandler.canRunDJAction(avaire, message, DJGuildLevel.NORMAL)) {
            return sendErrorMessage(message, "The `DJ` role is required to change the volume!");
        }

        if (!NumberUtil.isNumeric(args[0])) {
            return sendErrorMessage(message, "Invalid volume given, the volume must be a valid number between 1 and 100.");
        }

        int newVolume = NumberUtil.parseInt(args[0], -1);
        if ((newVolume < 0 || newVolume > 100)) {
            return sendErrorMessage(message, "Invalid volume given, the volume must between 1 and 100.");
        }

        musicManager.getPlayer().setVolume(newVolume);
        MessageFactory.makeSuccess(message, "\uD83C\uDFB5 Volume set to **:volume** volume\n:bar")
            .set("volume", newVolume)
            .set("bar", getVolumeString(newVolume, 18))
            .queue();

        return true;
    }

    private String getVolumeString(int volume, int multiplier) {
        StringBuilder volumeString = new StringBuilder();
        for (int i = 1; i <= multiplier; i++) {
            volumeString.append((i - 1) * (100 / multiplier) < volume ? "\u2592" : "\u2591");
        }
        return volumeString.toString();
    }
}

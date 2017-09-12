package com.avairebot.orion.commands.music;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.GuildMusicManager;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.List;

public class VolumeCommand extends AbstractCommand {

    public VolumeCommand(Orion orion) {
        super(orion);
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
                "`!volume` - Shows the current music volume without changing it",
                "`!volume <volume>` - Sets the music volume to the given number"
        );
    }

    @Override
    public String getExampleUsage() {
        return "`!volume 80`";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("volume", "vol");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(message, "Nothing is playing, request music first with `!play`");
        }

        int volume = musicManager.getPlayer().getVolume();

        if (args.length == 0) {
            MessageFactory.makeSuccess(message, "\uD83C\uDFB5 Music is playing at **%s** volume\n%s",
                    volume, getVolumeString(volume, 21)
            ).queue();
            return true;
        }

        try {
            int newVolume = Math.max(Math.min(Integer.parseInt(args[0], 10), 100), 0);

            musicManager.getPlayer().setVolume(newVolume);
            MessageFactory.makeSuccess(message, "\uD83C\uDFB5 Volume set to **%s** volume\n%s",
                    newVolume, getVolumeString(newVolume, 18)
            ).queue();

            return true;
        } catch (NumberFormatException ex) {
            return sendErrorMessage(message, "Invalid `volume` value given, the volume must be a number!");
        }
    }

    private String getVolumeString(int volume, int multiplier) {
        StringBuilder volumeString = new StringBuilder();
        for (int i = 1; i <= multiplier; i++) {
            volumeString.append((i - 1) * (100 / multiplier) < volume ? '▒' : '░');
        }
        return volumeString.toString();
    }
}

package com.avairebot.orion.commands.music;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.GuildMusicManager;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.permissions.Permissions;
import com.avairebot.orion.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VolumeCommand extends Command {

    public VolumeCommand(Orion orion) {
        super(orion, false);
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
    public String getExampleUsage() {
        return "`:command 80`";
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
            MessageFactory.makeSuccess(message, "\uD83C\uDFB5 Music is playing at **%s** volume\n%s",
                volume, getVolumeString(volume, 21)
            ).queue();
            return true;
        }

        if (!hasDJRole(message.getMember())) {
            return sendErrorMessage(message, "The `DJ` role is required to change the volume!");
        }

        try {
            int newVolume = NumberUtil.getBetween(NumberUtil.parseInt(args[0]), 0, 100);

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
            volumeString.append((i - 1) * (100 / multiplier) < volume ? "\u2592" : "\u2591");
        }
        return volumeString.toString();
    }

    private boolean hasDJRole(Member member) {
        if (member.hasPermission(Permissions.ADMINISTRATOR.getPermission())) {
            return true;
        }

        for (Role role : member.getRoles()) {
            if (role.getName().equalsIgnoreCase("DJ")) {
                return true;
            }
        }
        return false;
    }
}

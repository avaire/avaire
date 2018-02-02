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

public class SeekCommand extends Command {

    public SeekCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Seek Command";
    }

    @Override
    public String getDescription() {
        return "Jumps to the given time ";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <time>`");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command 2:24` - Jumps to 2 minutes 24 seconds of the song.");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("seek", "goto");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "has-dj-level:normal",
            "throttle:guild,2,4"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(context.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context, "Nothing is playing right now, request something with `!play` first.");
        }

        if (musicManager.getPlayer().getPlayingTrack().getInfo().isStream) {
            return sendErrorMessage(context, "You can not jump to a different time code for livestreams.");
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "Missing argument, the `number` argument is required.");
        }

        try {
            long time = NumberUtil.parseTimeString(args[0]);

            if (time > musicManager.getPlayer().getPlayingTrack().getDuration()) {
                return sendErrorMessage(context, "`%s` is more than the length of the current song playing, if you want to skip to the next song use `!skip` instead.", args[0]);
            }

            musicManager.getPlayer().getPlayingTrack().setPosition(time);

            context.makeSuccess("Seeking **:title** to `:time`")
                .set("title", musicManager.getPlayer().getPlayingTrack().getInfo().title)
                .set("time", NumberUtil.formatTime(time))
                .queue();
        } catch (IllegalStateException ex) {
            return sendErrorMessage(context, "The `number` argument must be a valid time format that is at least 0 or more seconds long.");
        }
        return true;
    }
}

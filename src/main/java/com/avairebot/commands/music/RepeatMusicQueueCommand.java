package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;

import java.util.Arrays;
import java.util.List;

public class RepeatMusicQueueCommand extends Command {

    public RepeatMusicQueueCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Repeat Music Command";
    }

    @Override
    public String getDescription() {
        return "Repeats all the songs in the music queue.";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("repeatsongs", "repeat", "loop");
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
            return sendErrorMessage(context, "There is nothing to repeat, request music first with `!play`");
        }

        musicManager.setRepeatQueue(!musicManager.isRepeatQueue());

        context.makeSuccess("Music queue looping has been turned `:status`.")
            .set("status", musicManager.isRepeatQueue() ? "ON" : "OFF")
            .queue();

        return true;
    }
}

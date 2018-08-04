package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.RestActionUtil;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        return Arrays.asList("repeatsongs", "repeatqueue", "loop");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:normal",
            "throttle:guild,2,4",
            "musicChannel"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context, context.i18n("error", generateCommandPrefix(context.getMessage())));
        }

        musicManager.setRepeatQueue(!musicManager.isRepeatQueue());


        context.makeSuccess(context.i18n("success"))
            .set("status", musicManager.isRepeatQueue()
                ? context.i18n("enabled") : context.i18n("disabled"))
            .queue(message -> message.delete().queueAfter(5, TimeUnit.MINUTES, null, RestActionUtil.ignore));

        return true;
    }
}

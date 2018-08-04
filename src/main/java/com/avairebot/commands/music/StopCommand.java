package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.audio.LavalinkManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.scheduler.tasks.MusicActivityTask;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StopCommand extends Command {

    public StopCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Stop Command";
    }

    @Override
    public String getDescription() {
        return "Stops the song currently playing, clears the music queue and disconnects from the voice channel the music was playing in.";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("stop");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:normal",
            "throttle:guild,1,5",
            "musicChannel"
        );
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context, context.i18n("error"));
        }

        String guildId = context.getGuild().getId();
        int size = musicManager.getScheduler().getQueue().size();

        musicManager.getPlayer().stopTrack();
        musicManager.getScheduler().getQueue().clear();

        MusicActivityTask.missingListener.remove(guildId);
        MusicActivityTask.playerPaused.remove(guildId);
        MusicActivityTask.emptyQueue.remove(guildId);

        if (LavalinkManager.LavalinkManagerHolder.lavalink.isEnabled()) {
            LavalinkManager.LavalinkManagerHolder.lavalink.getLavalink()
                .getLink(musicManager.getLastActiveMessage().getGuild()).destroy();
        }

        musicManager.getScheduler().nextTrack(false);

        context.makeInfo(context.i18n("success"))
            .set("number", size)
            .queue();

        return true;
    }
}

package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.audio.LavalinkManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.scheduler.MusicActivityJob;

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
            "has-dj-level:normal",
            "throttle:guild,1,5"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getGuildAudioPlayer(context.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context, context.i18n("error"));
        }

        String guildId = context.getGuild().getId();
        int size = musicManager.getScheduler().getQueue().size();

        musicManager.getPlayer().setPaused(true);
        musicManager.getScheduler().getQueue().clear();

        MusicActivityJob.MISSING_LISTENERS.remove(guildId);
        MusicActivityJob.PLAYER_PAUSED.remove(guildId);
        MusicActivityJob.EMPTY_QUEUE.remove(guildId);

        if (LavalinkManager.LavalinkManagerHolder.LAVALINK.isEnabled()) {
            LavalinkManager.LavalinkManagerHolder.LAVALINK.getLavalink()
                .getLink(musicManager.getLastActiveMessage().getGuild()).destroy();
        }

        musicManager.getScheduler().nextTrack(false);

        context.makeInfo(context.i18n("success"))
            .set("number", size)
            .queue();

        return true;
    }
}

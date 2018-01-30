package com.avairebot.scheduler;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.factories.MessageFactory;
import com.avairebot.shard.AvaireShard;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MusicActivityJob extends Job {

    public final static Map<Long, Integer> MISSING_LISTENERS = new HashMap<>();
    public final static Map<Long, Integer> EMPTY_QUEUE = new HashMap<>();
    public final static Map<Long, Integer> PLAYER_PAUSED = new HashMap<>();

    public MusicActivityJob(AvaIre avaire) {
        super(avaire, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        if (!avaire.getConfig().getBoolean("music-activity.enabled", true)) {
            return;
        }

        for (AvaireShard shard : avaire.getShards()) {
            Iterator<AudioManager> iterator = shard.getJDA().getAudioManagers().iterator();

            try {
                while (iterator.hasNext()) {
                    AudioManager manager = iterator.next();

                    if (!manager.isConnected()) {
                        continue;
                    }

                    long guildId = manager.getGuild().getIdLong();

                    if (!AudioHandler.MUSIC_MANAGER.containsKey(guildId)) {
                        handleEmptyMusic(manager, null, guildId);
                        continue;
                    }

                    GuildMusicManager guildMusicManager = AudioHandler.MUSIC_MANAGER.get(guildId);

                    if (guildMusicManager.getScheduler().getQueue().isEmpty() && guildMusicManager.getPlayer().getPlayingTrack() == null) {
                        handleEmptyMusic(manager, guildMusicManager, guildId);
                        continue;
                    }

                    if (EMPTY_QUEUE.containsKey(guildId)) {
                        EMPTY_QUEUE.remove(guildId);
                    }

                    if (guildMusicManager.getPlayer().isPaused()) {
                        handlePausedMusic(manager, guildMusicManager, guildId);
                        continue;
                    }

                    VoiceChannel voiceChannel = manager.getConnectedChannel();

                    boolean hasListeners = false;
                    for (Member member : voiceChannel.getMembers()) {
                        if (member.getUser().isBot()) {
                            continue;
                        }

                        if (member.getVoiceState().isDeafened()) {
                            continue;
                        }

                        hasListeners = true;
                        break;
                    }

                    if (hasListeners && !manager.getGuild().getSelfMember().getVoiceState().isMuted()) {
                        MISSING_LISTENERS.remove(guildId);
                        continue;
                    }

                    int times = MISSING_LISTENERS.getOrDefault(guildId, 0) + 1;

                    if (times <= getValue("missing-listeners", 5)) {
                        MISSING_LISTENERS.put(guildId, times);
                        continue;
                    }

                    clearItems(manager, guildMusicManager, guildId);
                }
            } catch (Exception e) {
                AvaIre.getLogger().error("An exception occurred during music activity job: " + e.getMessage(), e);
            }
        }
    }

    private void handleEmptyMusic(AudioManager manager, GuildMusicManager guildMusicManager, long guildId) {
        int times = EMPTY_QUEUE.getOrDefault(guildId, 0) + 1;

        if (times <= getValue("empty-queue-timeout", 2)) {
            EMPTY_QUEUE.put(guildId, times);
            return;
        }

        clearItems(manager, guildMusicManager, guildId);
    }

    private void handlePausedMusic(AudioManager manager, GuildMusicManager guildMusicManager, long guildId) {
        int times = PLAYER_PAUSED.getOrDefault(guildId, 0) + 1;

        if (times <= getValue("paused-music-timeout", 10)) {
            PLAYER_PAUSED.put(guildId, times);
            return;
        }

        clearItems(manager, guildMusicManager, guildId);
    }

    private void clearItems(AudioManager manager, GuildMusicManager guildMusicManager, long guildId) {
        if (guildMusicManager != null) {
            guildMusicManager.getScheduler().getQueue().clear();
            guildMusicManager.getPlayer().destroy();

            if (guildMusicManager.getLastActiveMessage() != null && guildMusicManager.getLastActiveMessage().getTextChannel().canTalk()) {
                MessageFactory.makeInfo(guildMusicManager.getLastActiveMessage(), "The music has ended due to inactivity.").queue();
            }
        }

        AudioHandler.MUSIC_MANAGER.remove(guildId);
        MISSING_LISTENERS.remove(guildId);
        PLAYER_PAUSED.remove(guildId);
        EMPTY_QUEUE.remove(guildId);

        manager.closeAudioConnection();
    }

    private int getValue(String path, int def) {
        return Math.max(1, avaire.getConfig()
            .getInt("music-activity" + path, def) * 2
        );
    }
}

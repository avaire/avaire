package com.avairebot.scheduler;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MusicActivityJob extends Job {

    private static Map<Long, Integer> MISSING_LISTENERS = new HashMap<>();

    public MusicActivityJob(AvaIre avaire) {
        super(avaire, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        Iterator<GuildMusicManager> iterator = AudioHandler.MUSIC_MANAGER.values().iterator();

        while (iterator.hasNext()) {
            GuildMusicManager manager = iterator.next();
            if (!isConnectedToVoice(manager.getLastActiveMessage())) {
                continue;
            }

            VoiceChannel voiceChannel = manager.getLastActiveMessage().getGuild().getAudioManager().getConnectedChannel();

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

            if (hasListeners && !manager.getLastActiveMessage().getGuild().getSelfMember().getVoiceState().isMuted()) {
                MISSING_LISTENERS.remove(manager.getLastActiveMessage().getGuild().getIdLong());
                continue;
            }

            int times = MISSING_LISTENERS.getOrDefault(manager.getLastActiveMessage().getGuild().getIdLong(), 0) + 1;

            if (times <= 6) {
                MISSING_LISTENERS.put(manager.getLastActiveMessage().getGuild().getIdLong(), times);
                continue;
            }

            MessageFactory.makeInfo(manager.getLastActiveMessage(), "The music has ended due to inactivity.").queue();

            MISSING_LISTENERS.remove(manager.getLastActiveMessage().getGuild().getIdLong());
            manager.getScheduler().getQueue().clear();
            manager.getLastActiveMessage().getGuild().getAudioManager().closeAudioConnection();
            iterator.remove();
        }
    }

    private boolean isConnectedToVoice(Message message) {
        return message != null && message.getGuild().getAudioManager().isConnected();
    }
}

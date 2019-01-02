/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.audio.cache;

import com.avairebot.audio.AudioTrackContainer;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.audio.LavalinkManager;
import com.avairebot.contracts.debug.Evalable;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class AudioState extends Evalable {

    private final List<AudioCache> queue = new ArrayList<>();
    private final AudioCache playingTrack;
    private final long guildId;
    private final long voiceChannelId;
    private final long messageChannelId;
    private final int volume;

    /**
     * Creates the audio state by copying the currently playing track, the volume,
     * voice and channel IDs used for the music, guild ID, as well as all the
     * tracks in the queue, the audio state can be used when the bot shuts
     * down, to save it all the cache, or when the bot starts back up
     * again, to load everything back into the music player.
     *
     * @param musicManager The guild music manager that the audio state should be created for.
     * @param guild        The JDA guild object for the current guild music manager.
     */
    public AudioState(GuildMusicManager musicManager, Guild guild) {
        this.volume = musicManager.getPlayer().getVolume();
        this.guildId = guild.getIdLong();

        VoiceChannel channel = LavalinkManager.LavalinkManagerHolder.lavalink.getConnectedChannel(guild);
        voiceChannelId = channel != null ? channel.getIdLong() : 0L;

        messageChannelId = musicManager.getLastActiveMessage() != null ?
            musicManager.getLastActiveMessage().getChannel().getIdLong() : 0L;

        AudioTrack playingTrack = musicManager.getPlayer().getPlayingTrack();
        AudioTrackContainer container = musicManager.getScheduler().getAudioTrackContainer();

        this.playingTrack = playingTrack == null ? null : new AudioCache(
            playingTrack.getInfo().uri,
            container != null ? container.getRequester().getIdLong() : 0L,
            playingTrack.getPosition()
        );

        musicManager.getScheduler().getQueue().stream()
            .map(AudioCache::new)
            .forEach(queue::add);
    }

    /**
     * The music queue of audio cache tracks, each track will hold the
     * audio track URL used to request the song originally, the ID of
     * the user who requested the track, and the position the track
     * was in before shutting down.
     *
     * @return The music queue of audio cache tracks.
     */
    public List<AudioCache> getQueue() {
        return queue;
    }

    /**
     * Gets the audio track that was playing at the time that the bot shut down.
     *
     * @return The audio track that was playing when the bot shut down.
     */
    @Nullable
    public AudioCache getPlayingTrack() {
        return playingTrack;
    }

    /**
     * Get ID of the guild that the audio state was created for.
     *
     * @return The ID of the guild that the audio state was created for.
     */
    public long getGuildId() {
        return guildId;
    }

    /**
     * Get ID of the voice channel that the audio state was created for.
     *
     * @return The ID of the voice channel that the audio state was created for.
     */
    public long getVoiceChannelId() {
        return voiceChannelId;
    }

    /**
     * Get ID of the message channel that the audio state was created for.
     *
     * @return The ID of the message channel that the audio state was created for.
     */
    public long getMessageChannelId() {
        return messageChannelId;
    }

    /**
     * The volume the music was playing at when the bot shut down.
     *
     * @return The volume the music was playing at when the bot shut down.
     */
    public int getVolume() {
        return volume;
    }
}

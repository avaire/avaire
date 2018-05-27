package com.avairebot.metrics.routes;

import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.AudioTrackContainer;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.audio.LavalinkManager;
import com.avairebot.contracts.metrics.SparkRoute;
import com.avairebot.metrics.Metrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.io.Link;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Map;

public class GetStats extends SparkRoute {

    public GetStats(Metrics metrics) {
        super(metrics);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        JSONObject root = new JSONObject();

        root.put("application", buildApplication());
        root.put("shards", buildShards());
        root.put("global", buildGlobal());
        root.put("music", buildMusic());

        return root;
    }

    private JSONObject buildApplication() {
        JSONObject app = new JSONObject();

        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        app.put("uptime", runtime.getUptime());
        app.put("startTime", runtime.getStartTime());
        app.put("memoryTotal", Runtime.getRuntime().totalMemory());
        app.put("memoryFree", Runtime.getRuntime().freeMemory());
        app.put("memoryMax", Runtime.getRuntime().maxMemory());
        app.put("availableProcessors", Runtime.getRuntime().availableProcessors());

        return app;
    }

    private JSONArray buildShards() {
        JSONArray shards = new JSONArray();

        for (JDA shard : metrics.getAvaire().getShardManager().getShards()) {
            JSONObject stats = new JSONObject();
            stats.put("id", shard.getShardInfo().getShardId())
                .put("guilds", shard.getGuilds().size())
                .put("users", shard.getUsers().size())
                .put("status", shard.getStatus())
                .put("channels", (shard.getTextChannels().size() + shard.getVoiceChannels().size()))
                .put("latency", shard.getPing());

            shards.put(stats);
        }

        return shards;
    }

    private JSONObject buildGlobal() {
        JSONObject global = new JSONObject();
        global.put("guilds", metrics.getAvaire().getShardEntityCounter().getGuilds());
        global.put("users", metrics.getAvaire().getShardEntityCounter().getUsers());

        JSONObject channels = new JSONObject();
        channels.put("total", metrics.getAvaire().getShardEntityCounter().getChannels());
        channels.put("text", metrics.getAvaire().getShardEntityCounter().getTextChannels());
        channels.put("voice", metrics.getAvaire().getShardEntityCounter().getVoiceChannels());

        global.put("channels", channels);

        return global;
    }

    private JSONObject buildMusic() {
        JSONObject music = new JSONObject();

        long seconds = 0;
        for (Map.Entry<Long, GuildMusicManager> entry : AudioHandler.getDefaultAudioHandler().musicManagers.entrySet()) {
            for (AudioTrackContainer track : entry.getValue().getScheduler().getQueue()) {
                if (track.getAudioTrack().getInfo().isStream) {
                    continue;
                }
                seconds += parseAudioTrackDuration(track.getAudioTrack()) / 1000L;
            }

            if (entry.getValue().getPlayer() == null) {
                continue;
            }

            seconds += parseAudioTrackDuration(entry.getValue().getPlayer().getPlayingTrack()) / 1000L;
        }

        int listeners = 0;
        if (LavalinkManager.LavalinkManagerHolder.LAVALINK.isEnabled()) {
            for (Link link : LavalinkManager.LavalinkManagerHolder.LAVALINK.getLavalink().getLinks()) {
                if (link.getChannel() != null) {
                    listeners += link.getChannel().getMembers().size();
                    continue;
                }
                listeners++;
            }
        } else {
            for (GuildMusicManager manager : AudioHandler.getDefaultAudioHandler().musicManagers.values()) {
                if (manager.getLastActiveMessage() == null) {
                    continue;
                }

                VoiceChannel connectedChannel = LavalinkManager.LavalinkManagerHolder.LAVALINK
                    .getConnectedChannel(manager.getLastActiveMessage().getGuild());
                if (connectedChannel != null) {
                    listeners += connectedChannel.getMembers().size();
                }
            }
        }

        music.put("servers", AudioHandler.getDefaultAudioHandler().getTotalListenersSize());
        music.put("queueSize", AudioHandler.getDefaultAudioHandler().getTotalQueueSize());
        music.put("listeners", listeners);
        music.put("queueTime", seconds);

        return music;
    }

    private long parseAudioTrackDuration(AudioTrack track) {
        if (track == null || track.getInfo().isStream) {
            return 0L;
        }
        return track.getDuration() - track.getPosition();
    }
}

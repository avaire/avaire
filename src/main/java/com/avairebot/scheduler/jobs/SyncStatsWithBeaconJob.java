package com.avairebot.scheduler.jobs;

import com.avairebot.AppInfo;
import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Job;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.SelfUser;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SyncStatsWithBeaconJob extends Job {

    private static final MediaType json = MediaType.parse("application/json; charset=utf-8");
    private static final Logger log = LoggerFactory.getLogger(SyncStatsWithBeaconJob.class);

    private final OkHttpClient client = new OkHttpClient();

    public SyncStatsWithBeaconJob(AvaIre avaire) {
        super(avaire, 5, 180, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        SelfUser selfUser = avaire.getShardManager().getShards().get(0).getSelfUser();

        Request.Builder request = new Request.Builder()
            .addHeader("User-Agent", "AvaIre v" + AppInfo.getAppInfo().version)
            .url("https://beacon.avairebot.com/v1/bot/" + selfUser.getId())
            .post(RequestBody.create(json, buildPayload(selfUser)));

        Response response = null;
        try {
            response = client.newCall(request.build()).execute();
        } catch (IOException e) {
            log.error("Failed sending sync with beacon request: " + e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private String buildPayload(SelfUser selfUser) {
        JSONObject main = new JSONObject();

        JSONObject bot = new JSONObject();
        bot.put("name", selfUser.getName());
        bot.put("avatar", selfUser.getAvatarId());
        main.put("bot", bot);

        JSONArray shards = new JSONArray();
        for (JDA shard : avaire.getShardManager().getShards()) {
            JSONObject shardObj = new JSONObject();

            shardObj.put("id", shard.getShardInfo().getShardId());
            shardObj.put("latency", shard.getPing());
            shardObj.put("users", shard.getUsers().size());
            shardObj.put("channels", getTotalChannels(shard));
            shardObj.put("guilds", shard.getGuilds().size());

            shards.put(shardObj);
        }
        main.put("shards", shards);

        return main.toString();
    }

    private int getTotalChannels(JDA jda) {
        return jda.getTextChannels().size()
            + jda.getVoiceChannels().size();
    }
}

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

package com.avairebot.servlet.routes;

import com.avairebot.AppInfo;
import com.avairebot.AvaIre;
import com.avairebot.GitInfo;
import com.avairebot.contracts.metrics.SparkRoute;
import net.dv8tion.jda.core.JDA;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class GetStats extends SparkRoute {

    @Override
    public Object handle(Request request, Response response) throws Exception {
        JSONObject root = new JSONObject();

        root.put("application", buildApplication());
        root.put("shards", buildShards());
        root.put("global", buildGlobal());

        try {
            root.put("build", buildBuildInformation());
        } catch (NullPointerException ignored) {
            // The AppInfo configuration class will throw a NPE if the project is
            // not built using Gradle with the gradle-git-properties plugin,
            // which is only the case during development.
        }

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

        for (JDA shard : AvaIre.getInstance().getShardManager().getShards()) {
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
        global.put("guilds", AvaIre.getInstance().getShardEntityCounter().getGuilds());
        global.put("users", AvaIre.getInstance().getShardEntityCounter().getUsers());

        JSONObject channels = new JSONObject();
        channels.put("total", AvaIre.getInstance().getShardEntityCounter().getChannels());
        channels.put("text", AvaIre.getInstance().getShardEntityCounter().getTextChannels());
        channels.put("voice", AvaIre.getInstance().getShardEntityCounter().getVoiceChannels());

        global.put("channels", channels);

        return global;
    }

    private JSONObject buildBuildInformation() throws NullPointerException {
        JSONObject build = new JSONObject();

        JSONObject app = new JSONObject();
        app.put("version", AppInfo.getAppInfo().version);
        app.put("groupId", AppInfo.getAppInfo().groupId);
        app.put("artifactId", AppInfo.getAppInfo().artifactId);

        build.put("app", app);

        JSONObject git = new JSONObject();
        git.put("branch", GitInfo.getGitInfo().branch);
        git.put("commitId", GitInfo.getGitInfo().commitId);
        git.put("commitTime", GitInfo.getGitInfo().commitTime);
        build.put("git", git);

        return build;
    }
}

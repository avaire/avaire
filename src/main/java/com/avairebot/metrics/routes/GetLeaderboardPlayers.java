package com.avairebot.metrics.routes;

import com.avairebot.Constants;
import com.avairebot.commands.utility.LeaderboardCommand;
import com.avairebot.contracts.metrics.SparkRoute;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.metrics.Metrics;
import com.avairebot.utilities.CacheUtil;
import net.dv8tion.jda.core.entities.Guild;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.sql.SQLException;

public class GetLeaderboardPlayers extends SparkRoute {

    public GetLeaderboardPlayers(Metrics metrics) {
        super(metrics);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        try {
            Long guildId = Long.parseLong(request.params("id"));

            Guild guild = metrics.getAvaire().getShardManager().getGuildById(guildId);
            if (guild == null) {
                return buildResponse(response, 404, "Invalid guild ID given, no guild found with the given id.");
            }

            GuildTransformer transformer = GuildController.fetchGuild(metrics.getAvaire(), guild);

            JSONObject root = new JSONObject();
            root.put("id", guild.getId());
            root.put("name", guild.getName());
            root.put("enabled", transformer.isLevels());

            JSONArray users = new JSONArray();
            if (transformer.isLevels()) {
                for (DataRow row : loadTop100(guildId.toString())) {
                    JSONObject user = new JSONObject();
                    user.put("id", row.getString("user_id"));
                    user.put("username", row.getString("username"));
                    user.put("rawUsername", row.getRaw().get("username"));
                    user.put("discriminator", row.getString("discriminator"));
                    user.put("avatar", row.getString("avatar"));
                    user.put("experience", row.getLong("experience"));

                    users.put(user);
                }
            }
            root.put("leaderboard", users);

            return root;
        } catch (NumberFormatException e) {
            return buildResponse(response, 400, "Invalid guild ID given, the ID must be a number.");
        } catch (RuntimeException e) {
            return buildResponse(response, 404, "Invalid guild ID given, no guild found with the given id.");
        }
    }

    private Collection loadTop100(String guildId) {
        return (Collection) CacheUtil.getUncheckedUnwrapped(LeaderboardCommand.cache, guildId, () -> {
            try {
                return metrics.getAvaire().getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                    .where("guild_id", guildId)
                    .orderBy("experience", "desc")
                    .take(100)
                    .get();
            } catch (SQLException e) {
                return null;
            }
        });
    }
}

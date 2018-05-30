package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.cache.CacheType;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.CacheFingerprint;
import com.avairebot.contracts.commands.ThreadCommand;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.utilities.LevelUtil;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CacheFingerprint(name = "leaderboard-command")
public class GlobalLeaderboardCommand extends ThreadCommand {

    public GlobalLeaderboardCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Global Leaderboard Command";
    }

    @Override
    public String getDescription() {
        return "Shows the top 100 users globally, combining their rank, level, and xp between all servers the users are on.";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("gleaderboard", "gtop");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:channel,2,5");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        Collection collection = loadTop100From();
        if (collection == null) {
            context.makeWarning(context.i18n("noData")).queue();
            return false;
        }

        List<String> messages = new ArrayList<>();
        SimplePaginator paginator = new SimplePaginator(collection.getItems(), 10);
        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        paginator.forEach((index, key, val) -> {
            DataRow row = (DataRow) val;

            Member member = context.getGuild().getMemberById(row.getLong("user_id"));
            String username = row.getString("username") + "#" + row.getString("discriminator");
            if (member != null) {
                username = member.getUser().getName() + "#" + member.getUser().getDiscriminator();
            }

            long experience = row.getLong("total", 100);

            messages.add(context.i18n("line")
                .replace(":num", "" + (index + 1))
                .replace(":username", username)
                .replace(":level", "" + LevelUtil.getLevelFromExperience(experience))
                .replace(":experience", "" + (experience - 100))
            );
        });

        messages.add("\n" + paginator.generateFooter(generateCommandTrigger(context.getMessage())));

        context.makeEmbeddedMessage(null, String.join("\n", messages))
            .setTitle(context.i18n("title")).queue();

        return true;
    }

    private Collection loadTop100From() {
        return (Collection) avaire.getCache().getAdapter(CacheType.MEMORY).remember("database-xp-leaderboard.global", 300, () -> {
            try {
                return avaire.getDatabase().query("SELECT " +
                    "`user_id`, `username`, `discriminator`, (sum(`experience`) - (count(`user_id`) * 100)) + 100 as `total` " +
                    "FROM `experiences` " +
                    "GROUP BY `user_id` " +
                    "ORDER BY `total` DESC " +
                    "LIMIT 100;"
                );
            } catch (SQLException e) {
                AvaIre.getLogger().error("Failed to fetch global leaderboard data", e);
                return null;
            }
        });
    }
}

package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.chat.SimplePaginator;
import com.avairebot.orion.contracts.commands.CacheFingerprint;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.database.collection.Collection;
import com.avairebot.orion.database.collection.DataRow;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.LevelUtil;
import com.avairebot.orion.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CacheFingerprint(name = "leaderboard-command")
public class GlobalLeaderboardCommand extends Command {

    public GlobalLeaderboardCommand(Orion orion) {
        super(orion, false);
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
    public List<String> getUsageInstructions() {
        return null;
    }

    @Override
    public String getExampleUsage() {
        return null;
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
    public boolean onCommand(Message message, String[] args) {
        Collection collection = loadTop100From();
        if (collection == null) {
            MessageFactory.makeWarning(message, "There are no leaderboard data right now, try again later.").queue();
            return false;
        }

        List<String> messages = new ArrayList<>();
        SimplePaginator paginator = new SimplePaginator(collection.getItems(), 10);
        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        paginator.forEach((index, key, val) -> {
            DataRow row = (DataRow) val;

            Member member = message.getGuild().getMemberById(row.getLong("user_id"));
            String username = row.getString("username") + "#" + row.getString("discriminator");
            if (member != null) {
                username = member.getUser().getName() + "#" + member.getUser().getDiscriminator();
            }

            long experience = row.getLong("total", 100);

            messages.add(String.format("`%s` **%s** is level **%s** with **%s** xp.",
                index + 1,
                username,
                LevelUtil.getLevelFromExperience(experience),
                experience - 100
            ));
        });

        messages.add("\n" + paginator.generateFooter(generateCommandTrigger(message)));

        MessageFactory.makeEmbeddedMessage(message.getChannel(), null, String.join("\n", messages))
            .setTitle("Global Experience Leaderboard").queue();

        return true;
    }

    private Collection loadTop100From() {
        return (Collection) orion.getCache().getAdapter(CacheType.MEMORY).remember("database-xp-leaderboard.global", 300, () -> {
            return orion.getDatabase().query("SELECT " +
                "`user_id`, `username`, `discriminator`, sum(`experience`) - (count(`user_id`) * 100) as `total` " +
                "FROM `experiences` " +
                "GROUP BY `user_id` " +
                "ORDER BY `total` DESC " +
                "LIMIT 100;"
            );

        });
    }
}

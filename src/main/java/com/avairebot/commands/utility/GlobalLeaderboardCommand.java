package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.CacheFingerprint;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.utilities.CacheUtil;
import com.avairebot.utilities.LevelUtil;
import com.avairebot.utilities.NumberUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@CacheFingerprint(name = "leaderboard-command")
public class GlobalLeaderboardCommand extends Command {

    public static final Cache<String, Collection> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(300, TimeUnit.SECONDS)
        .build();

    private static final Logger log = LoggerFactory.getLogger(GlobalLeaderboardCommand.class);

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
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            RankCommand.class,
            LeaderboardCommand.class
        );
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
        if (cache.asMap().containsKey("leaderboard")) {
            return handleCommand(context, args, null);
        }

        context.makeInfo(context.i18n("loading.message"))
            .setTitle(context.i18n("loading.title"))
            .queue(message -> handleCommand(context, args, message), error -> {
                log.error("Failed to load the global leaderboard: {}", error.getMessage(), error);
            });

        return true;
    }

    private boolean handleCommand(@Nonnull CommandMessage context, @Nonnull String[] args, @Nullable Message loadingMessage) {
        Collection collection = loadTop100From();
        if (collection == null) {
            if (loadingMessage != null) {
                loadingMessage.delete().queue();
            }
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
                .replace(":level", NumberUtil.formatNicely(LevelUtil.getLevelFromExperience(experience)))
                .replace(":experience", NumberUtil.formatNicely(experience - 100))
            );
        });

        PlaceholderMessage message = context.makeEmbeddedMessage(null, String.join("\n", messages))
            .setTitle("\uD83C\uDFC6 " + context.i18n("title"))
            .requestedBy(context.getMember());

        Collection userRank = loadUserRank(context);
        if (userRank != null && !userRank.isEmpty()) {
            int rank = userRank.first().getInt("rank", -1);
            if (++rank > 0) {
                Collection userXp = loadUserXp(context);
                if (userXp != null && !userXp.isEmpty()) {
                    long experience = userXp.first().getLong("total");
                    message.addField("➡ " + context.i18n("yourRank"), context.i18n("line")
                            .replace(":num", NumberUtil.formatNicely(rank))
                            .replace(":username", context.getAuthor().getName() + "#" + context.getAuthor().getDiscriminator())
                            .replace(":level", NumberUtil.formatNicely(LevelUtil.getLevelFromExperience(experience)))
                            .replace(":experience", NumberUtil.formatNicely(experience - 100))
                            + "\n\n" + paginator.generateFooter(generateCommandTrigger(context.getMessage())),
                        false
                    );
                }
            }
        }

        if (message.build().getFields().isEmpty()) {
            messages.add("\n" + paginator.generateFooter(generateCommandTrigger(context.getMessage())));
            message.setDescription(String.join("\n", messages));
        }

        if (loadingMessage != null) {
            loadingMessage.editMessage(message.buildEmbed()).queue();
        } else {
            message.queue();
        }

        return true;
    }

    private Collection loadTop100From() {
        return (Collection) CacheUtil.getUncheckedUnwrapped(cache, "leaderboard", () -> {
            try {
                return avaire.getDatabase().query("SELECT " +
                    "`user_id`, `username`, `discriminator`, (sum(`experience`) - (count(`user_id`) * 100)) + 100 as `total` " +
                    "FROM `experiences` " +
                    "GROUP BY `user_id` " +
                    "ORDER BY `total` DESC " +
                    "LIMIT 100;"
                );
            } catch (SQLException e) {
                log.error("Failed to fetch global leaderboard data", e);
                return null;
            }
        });
    }

    private Collection loadUserRank(CommandMessage context) {
        return (Collection) CacheUtil.getUncheckedUnwrapped(cache, "user.rank." + context.getAuthor().getId(), () -> {
            try {
                return avaire.getDatabase().query(String.format(
                    "SELECT COUNT(*) AS rank FROM (" +
                        "    SELECT `user_id` FROM `experiences` GROUP BY `user_id` HAVING SUM(`experience`) > (" +
                        "        SELECT SUM(`experience`) FROM `experiences` WHERE `user_id` = '%s'" +
                        "    )" +
                        ") t;",
                    context.getAuthor().getId()
                ));
            } catch (SQLException e) {
                log.error("Failed to fetch leaderboard data for user: " + context.getGuild().getId(), e);
                return null;
            }
        });
    }

    private Collection loadUserXp(CommandMessage context) {
        return (Collection) CacheUtil.getUncheckedUnwrapped(cache, "user.xp." + context.getAuthor().getId(), () -> {
            try {
                return avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                    .selectRaw("(sum(`experience`) - (count(`user_id`) * 100)) + 100 as `total`")
                    .where("user_id", context.getAuthor().getIdLong())
                    .get();
            } catch (SQLException e) {
                log.error("Failed to fetch leaderboard data for user: " + context.getGuild().getId(), e);
                return null;
            }
        });
    }
}

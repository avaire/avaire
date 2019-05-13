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

package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.administration.LevelCommand;
import com.avairebot.contracts.commands.CacheFingerprint;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.CacheUtil;
import com.avairebot.utilities.NumberUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@CacheFingerprint(name = "leaderboard-command")
public class LeaderboardCommand extends Command {

    public static final Cache<String, Collection> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(60, TimeUnit.SECONDS)
        .build();

    private static final Logger log = LoggerFactory.getLogger(GlobalLeaderboardCommand.class);

    public LeaderboardCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Leaderboard Command";
    }

    @Override
    public String getDescription() {
        return "Displays the server's level leaderboard with the user's name, rank, level and XP. The response is paginated to show 10 users per page.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Displays the top 100 players on the XP leaderboard for the server.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command 2`");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            RankCommand.class,
            GlobalLeaderboardCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("leaderboard", "top");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:channel,2,5");
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.LEVEL_AND_EXPERIENCE);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null || !transformer.isLevels()) {
            return sendErrorMessage(
                context,
                "errors.requireLevelFeatureToBeEnabled",
                CommandHandler.getCommand(LevelCommand.class)
                    .getCommand().generateCommandTrigger(context.getMessage())
            );
        }

        Collection collection = loadTop100From(context);
        if (collection == null) {
            context.makeWarning(context.i18n("noData")).queue();
            return false;
        }

        List<String> messages = new ArrayList<>();
        SimplePaginator<DataRow> paginator = new SimplePaginator<>(collection.getItems(), 10);
        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        long zeroExperience = avaire.getLevelManager().getExperienceFromLevel(transformer, 0) - 100;

        paginator.forEach((index, key, row) -> {
            Member member = context.getGuild().getMemberById(row.getLong("user_id"));
            String username = row.getString("username") + "#" + row.getString("discriminator");
            if (member != null) {
                username = member.getUser().getName() + "#" + member.getUser().getDiscriminator();
            }

            long experience = row.getLong("experience", 100);

            messages.add(context.i18n("line")
                .replace(":num", "" + (index + 1))
                .replace(":username", username)
                .replace(":level", NumberUtil.formatNicely(
                    avaire.getLevelManager().getLevelFromExperience(transformer, experience + zeroExperience)
                ))
                .replace(":experience", NumberUtil.formatNicely(experience - 100))
            );
        });

        PlaceholderMessage message = context.makeInfo(String.join("\n", messages))
            .setTitle("\uD83C\uDFC6 " +
                    context.i18n("title", context.getGuild().getName()),
                "https://avairebot.com/leaderboard/" + context.getGuild().getId()
            )
            .requestedBy(context.getMember());

        Collection userRank = loadUserRank(context);
        if (userRank != null && !userRank.isEmpty()) {
            int rank = userRank.first().getInt("rank", -1);
            if (++rank > 0) {
                message.addField("➡ " + context.i18n("yourRank"), context.i18n("line")
                        .replace(":num", NumberUtil.formatNicely(rank))
                        .replace(":username", context.getAuthor().getName() + "#" + context.getAuthor().getDiscriminator())
                        .replace(":level", NumberUtil.formatNicely(avaire.getLevelManager().getLevelFromExperience(
                            context.getGuildTransformer(), context.getPlayerTransformer().getExperience() + zeroExperience
                        )))
                        .replace(":experience", NumberUtil.formatNicely(context.getPlayerTransformer().getExperience() - 100))
                        + "\n\n" + paginator.generateFooter(context.getGuild(), generateCommandTrigger(context.getMessage())),
                    false
                );
            }
        }

        if (message.build().getFields().isEmpty()) {
            messages.add("\n" + paginator.generateFooter(context.getGuild(), generateCommandTrigger(context.getMessage())));
            message.setDescription(String.join("\n", messages));
        }

        message.queue();

        return true;
    }

    private Collection loadTop100From(CommandMessage context) {
        return (Collection) CacheUtil.getUncheckedUnwrapped(cache, asKey(context, false), () -> {
            try {
                return avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                    .where("guild_id", context.getGuild().getId())
                    .where("active", 1)
                    .orderBy("experience", "desc")
                    .take(100)
                    .get();
            } catch (SQLException e) {
                log.error("Failed to fetch leaderboard data for server: " + context.getGuild().getId(), e);
                return null;
            }
        });
    }

    private Collection loadUserRank(CommandMessage context) {
        return (Collection) CacheUtil.getUncheckedUnwrapped(cache, asKey(context, true), () -> {
            try {
                return avaire.getDatabase().query(String.format(
                    "SELECT COUNT(*) AS rank FROM (" +
                        "    SELECT `user_id` FROM `experiences` WHERE `guild_id` = '%s' AND `active` = 1 GROUP BY `user_id` HAVING SUM(`experience`) > (" +
                        "        SELECT SUM(`experience`) FROM `experiences` WHERE `user_id` = '%s' AND `guild_id` = '%s' AND `active` = 1" +
                        "    )" +
                        ") t;",
                    context.getGuild().getId(), context.getAuthor().getId(), context.getGuild().getId()
                ));
            } catch (SQLException e) {
                log.error("Failed to fetch leaderboard data for user: " + context.getGuild().getId(), e);
                return null;
            }
        });
    }

    private String asKey(CommandMessage context, boolean isUser) {
        return context.getGuild().getId() + (isUser ? "." + context.getAuthor().getId() : "");
    }
}

/*
 * Copyright (c) 2020.
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
package com.avairebot.commands.economy;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.CacheFingerprint;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.controllers.EconomyController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.CacheUtil;
import com.avairebot.utilities.NumberUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@CacheFingerprint(name = "baltop-command")
public class BaltopCommand extends Command
{

    public static final Cache<String, Collection> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(60, TimeUnit.SECONDS)
        .build();

    private static final Logger log = LoggerFactory.getLogger(BaltopCommand.class);

    public BaltopCommand(AvaIre avaire) {
        super(avaire, false);
    }


    /**
     * Gets the command name, this is used in help and error
     * messages as the title as well as log messages.
     *
     * @return Never-null, the command name.
     */
    @Override
    public String getName() {
        return "Baltop Command";
    }


    @Override
    public String getDescription() {
        return "Displays the server's economy leaderboard with the user's name and balance. The response is paginated to show 10 users per page.";
    }
    /**
     * Gets am immutable list of command triggers that can be used to invoke the current
     * command, the first index in the list will be used when the `:command` placeholder
     * is used in {@link #getDescription(CommandContext)} or {@link #getUsageInstructions()} methods.
     *
     * @return An immutable list of command triggers that should invoked the command.
     */
    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("baltop");
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Displays the top 100 players on the economy leaderboard for the server.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command 2`");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:channel,2,5");
    }

    /**
     * The command executor, this method is invoked by the command handler
     * and the middleware stack when a user sends a message matching the
     * commands prefix and one of its command triggers.
     *
     * @param context The command message context generated using the
     *                JDA message event that invoked the command.
     * @param args    The arguments given to the command, if no arguments was given the array will just be empty.
     * @return true on success, false on failure.
     */
    @Override
    public boolean onCommand(CommandMessage context, String[] args)
    {
        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
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


        paginator.forEach((index, key, row) -> {
            Member member = context.getGuild().getMemberById(row.getLong("user_id"));
            String username = "";
            if (member != null) {
                username = member.getUser().getName() + "#" + member.getUser().getDiscriminator();
            }

            long balance = row.getLong("balance", 100);

            messages.add(context.i18n("line")
                .replace(":num", "" + (index + 1))
                .replace(":username", username)
                .replace(":balance", NumberUtil.formatNicely(balance))
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

                        .replace(":balance", NumberUtil.formatNicely(
                            EconomyController.fetchEconomy(avaire,context.getMessage(),context.getAuthor().getIdLong())
                                .getBalance()))
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
                return avaire.getDatabase().newQueryBuilder(Constants.ECONOMY_TABLE_NAME)
                    .where("guild_id", context.getGuild().getId())
                    .where("active", true)
                    .orderBy("balance", "desc")
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
                        "    SELECT `user_id` FROM `economy` WHERE `guild_id` = '%s' AND `active` = 'true' GROUP BY `user_id` HAVING SUM(`balance`) > (" +
                        "        SELECT SUM(`balance`) FROM `experiences` WHERE `user_id` = '%s' AND `guild_id` = '%s' AND `active` = 'true'" +
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
        return context.getGuild().getId() + (isUser ? ":" + context.getAuthor().getId() : "");
    }
}

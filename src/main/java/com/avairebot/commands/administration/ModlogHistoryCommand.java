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

package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.language.I18n;
import com.avairebot.modlog.ModlogType;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModlogHistoryCommand extends Command {

    public ModlogHistoryCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Modlog History Command";
    }

    @Override
    public String getDescription() {
        return "Displays the modlog history for the mentioned user, this will display all past warnings, bans, soft bans, kicks, and voice kicks.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <user>` - Displays the modlog history for the mentioned user."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "`:command @Senither` - Displays all the bad things Senither has done."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            ModlogCommand.class,
            ModlogReasonCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("modloghistory", "history");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,text.manage_messages",
            "throttle:channel,1,5"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MODERATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        User user = MentionableUtil.getUser(context, args);
        if (user == null) {
            return sendErrorMessage(context, context.i18n("mustMentionUser"));
        }

        try {
            Collection items = avaire.getDatabase().newQueryBuilder(Constants.LOG_TABLE_NAME)
                .where("guild_id", context.getGuild().getId())
                .where("target_id", user.getId())
                .where("pardon", 0)
                .get();

            if (items.isEmpty()) {
                context.makeWarning(context.i18n("noHistory"))
                    .setTitle(context.i18n("title",
                        user.getName(), user.getDiscriminator(), 0
                    ))
                    .queue();

                return true;
            }

            List<String> records = new ArrayList<>();
            items.forEach(row -> {
                ModlogType type = ModlogType.fromId(row.getInt("type", 0));
                String reason = row.getString("reason", context.i18n("noReasonGiven"));

                Carbon time = row.getTimestamp("created_at");

                String modlogCaseId = context.getGuildTransformer() == null
                    ? row.getString("modlogCase")
                    : I18n.format("[{0}](https://discordapp.com/channels/{1}/{2}/{3})",
                    row.getString("modlogCase"),
                    context.getGuild().getIdLong(),
                    context.getGuildTransformer().getModlog(),
                    row.getString("message_id")
                );

                records.add(context.i18n("entry",
                    modlogCaseId,
                    type == null ? "Unknown" : type.getEmote() + " " + type.getName(),
                    formatUser(avaire.getShardManager().getUserById(row.getLong("user_id")), row),
                    time == null ? "Unknown" : time.format("EEE, MMM dd, yyyy h:mm aaa z"),
                    reason
                ));
            });

            List<String> messages = new ArrayList<>();
            SimplePaginator<String> paginator = new SimplePaginator<>(records, 5);
            if (args.length > 1) {
                paginator.setCurrentPage(NumberUtil.parseInt(args[1], 1));
            }

            paginator.forEach((_index, _key, val) -> messages.add(val));
            messages.add("\n" + paginator.generateFooter(context.getGuild(), generateCommandTrigger(context.getMessage())));

            context.makeInfo(String.join("\n", messages))
                .setTitle(context.i18n("title",
                    user.getName(), user.getDiscriminator(), paginator.getTotal()
                ))
                .queue();
        } catch (SQLException e) {
            AvaIre.getLogger().error("Failed to load log records for user {} in guild {}",
                user.getId(), context.getGuild().getId(), e
            );

            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "modlog records");
        }

        return true;
    }

    private String formatUser(User user, DataRow row) {
        if (user == null) {
            return "ID:" + row.getString("user_id");
        }
        return user.getName() + "#" + user.getDiscriminator();
    }
}

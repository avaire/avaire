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
import com.avairebot.commands.Category;
import com.avairebot.commands.CategoryHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.ComparatorUtil;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.*;

public class ToggleCategoryCommand extends Command {

    public ToggleCategoryCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Toggle Category Command";
    }

    @Override
    public String getDescription() {
        return "This command allows you to toggle command categories on/off for the " +
            "current channel or the whole server in one go, this is useful if you " +
            "like some features in the bot but not others.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <category> <channel/global> [status]` - Changes the command " +
                "category status for the mentioned channel or globally if specified."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command fun global off` - Disables all the fun on the server D:",
            "`:command util #general off` - Disables all the utility commands in the general channel."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(CategoriesCommand.class, ChangePrefixCommand.class);
    }


    @Override
    public List<String> getTriggers() {
        return Arrays.asList("togglecategory", "tcategory", "tcat");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "throttle:guild,1,4",
            "require:user,general.administrator"
        );
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.LOW;
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.COMMAND_CUSTOMIZATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length < 1) {
            return sendErrorMessage(context, "errors.missingArgument", "category");
        }

        Category category = CategoryHandler.fromLazyName(args[0]);
        if (category == null || category.isGlobalOrSystem()) {
            return sendErrorMessage(context, context.i18n("invalidCategory"), args[0]);
        }

        if (args.length < 2) {
            return sendErrorMessage(context, context.i18n("missingArgumentType"));
        }

        if (!args[1].equalsIgnoreCase("global") && context.getMessage().getMentionedChannels().size() != 1) {
            return sendErrorMessage(context, context.i18n("invalidChannelOrGlobalString"));
        }

        String channelId = args[1].equalsIgnoreCase("global") ?
            "all" : context.getMessage().getMentionedChannels().get(0).getId();

        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        ChannelTransformer channel = transformer.getChannel(channelId);

        boolean status = !channel.isCategoryEnabled(category);
        if (args.length > 2) {
            ComparatorUtil.ComparatorType type = ComparatorUtil.getFuzzyType(args[2]);
            if (!type.equals(ComparatorUtil.ComparatorType.UNKNOWN)) {
                status = type.getValue();
            }
        }

        if (!channelId.equals("all") && status && !channel.isCategoryEnabledGlobally(category)) {
            context.makeError(context.i18n("cantEnabledCategory"))
                .set("command", generateCommandTrigger(context.getMessage()))
                .set("category", category.getName())
                .queue();

            return false;
        }

        if (!transformer.getCategories().containsKey(channelId)) {
            transformer.getCategories().put(channelId, new HashMap<>());
        }

        transformer.getCategories().get(channelId).
            put(category.getName().toLowerCase(), status ? "true" : "false");

        if (channelId.equals("all")) {
            for (Map.Entry<String, Map<String, String>> item : transformer.getCategories().entrySet()) {
                if (item.getKey().equalsIgnoreCase("all")) {
                    continue;
                }

                if (item.getValue().containsKey(category.getName().toLowerCase())) {
                    transformer.getCategories().get(item.getKey()).remove(category.getName().toLowerCase());
                }
            }
        }

        try {
            updateGuildCategories(context.getMessage(), transformer);

            context.makeSuccess(getStatusMessage(context, channelId))
                .set("category", category.getName())
                .set("channel", "<#" + channel.getId() + ">")
                .set("status", context.i18n("status." + (status ? "enabled" : "disabled")))
                .queue();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void updateGuildCategories(Message context, GuildTransformer transformer) throws SQLException {
        avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
            .where("id", context.getGuild().getId())
            .update(statement -> {
                statement.set("modules", AvaIre.gson.toJson(transformer.getCategories()));
            });
    }

    private String getStatusMessage(CommandMessage context, String channelId) {
        return channelId.equals("all")
            ? context.i18n("update.globally")
            : context.i18n("update.category");
    }
}

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
import com.avairebot.database.transformers.GuildTransformer;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChangePrefixCommand extends Command {

    public ChangePrefixCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Change Prefix Command";
    }

    @Override
    public String getDescription() {
        return "Sets the prefix that should be used for all commands in a given category, if no prefix is provided the category prefix will be reset back to the default instead, each category in AvaIre can have a different prefix, or you can choose to change them all at the same time.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <category>` - Resets the category prefix back to its default prefix.",
            "`:command <category> [prefix]` - Sets the category prefix to the given prefix.");
    }

    @Override
    public List<String> getExampleUsage() {
        if (avaire.getSettings().isMusicOnlyMode()) {
            return Arrays.asList(
                "`:command all a!` - Sets the prefix for all commands to `a!`.",
                "`:command all` - Resets the prefix back to using the default."
            );
        }

        return Arrays.asList(
            "`:command fun` - Resets the prefix back to default for the `fun` commands.",
            "`:command admin /` - Sets the prefix to `/` for all admin commands.",
            "`:command all a!` - Sets the prefix for all the categories to `a!`."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(CategoriesCommand.class, ToggleCategoryCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("changeprefix", "prefix");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.administrator",
            "throttle:guild,1,5"
        );
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.HIGHEST;
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.COMMAND_CUSTOMIZATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, context.i18n("missingArgument"));
        }

        Category category = CategoryHandler.fromLazyName(args[0], true);
        if (category == null) {
            return sendErrorMessage(context, context.i18n("invalidCategory", args[0]));
        }

        GuildTransformer transformer = context.getGuildTransformer();
        if (args.length == 1) {
            return removeCustomPrefix(context, transformer, category);
        }

        String prefix = args[1];
        if (prefix.contains(" ") || prefix.length() < 1 || prefix.length() > 16) {
            return sendErrorMessage(context, context.i18n("invalidPrefix", prefix));
        }

        try {
            setCustomPrefix(transformer, category, prefix);
            updateGuildPrefixes(context, transformer);

            context.makeSuccess(!category.isGlobal()
                ? context.i18n("update.category")
                : context.i18n("update.global")
            )
                .set("category", category.getName())
                .set("prefix", prefix)
                .queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void setCustomPrefix(GuildTransformer transformer, Category category, String prefix) {
        if (!category.isGlobal()) {
            transformer.getPrefixes().put(category.getName().toLowerCase(), prefix);
            return;
        }

        for (Category cat : CategoryHandler.getValues()) {
            if (cat.isGlobal()) continue;

            transformer.getPrefixes().put(cat.getName().toLowerCase(), prefix);
        }
    }

    private boolean removeCustomPrefix(CommandMessage context, GuildTransformer transformer, Category category) {
        removeCustomPrefix(transformer, category);

        try {
            updateGuildPrefixes(context, transformer);

            context.makeSuccess(!category.isGlobal()
                ? context.i18n("reset.category")
                : context.i18n("reset.global")
            )
                .set("category", category.getName())
                .set("prefix", category.getPrefix())
                .queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void removeCustomPrefix(GuildTransformer transformer, Category category) {
        if (!category.isGlobal()) {
            transformer.getPrefixes().remove(category.getName().toLowerCase());
            return;
        }

        for (Category cat : CategoryHandler.getValues()) {
            if (cat.isGlobal()) continue;

            transformer.getPrefixes().remove(cat.getName().toLowerCase());
        }
    }

    private void updateGuildPrefixes(CommandMessage context, GuildTransformer transformer) throws SQLException {
        avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
            .where("id", context.getGuild().getId())
            .update(statement -> {
                statement.set("prefixes", AvaIre.gson.toJson(transformer.getPrefixes()), true);
            });
    }
}

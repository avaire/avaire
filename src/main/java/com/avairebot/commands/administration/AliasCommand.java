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
import com.avairebot.commands.CommandContainer;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.transformers.GuildTransformer;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AliasCommand extends Command {

    public AliasCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Alias Command";
    }

    @Override
    public String getDescription() {
        return "Creates and maps a custom alias for a pre-existing command. Provide no alias to remove an existing alias.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <alias>` - Deletes the alias if it exists.",
            "`:command <alias> <command>` - Creates an alias for the given command."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command !ava !repeat **Website:** https://avairebot.com/`");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(ListAliasesCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("alias", "cmdmap");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "throttle:user,2,5",
            "require:user,general.manage_server"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.COMMAND_CUSTOMIZATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "alias");
        }

        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        if (args.length == 1) {
            return removeCustomAlias(context, transformer, args);
        }

        if (transformer.getAliases().containsKey(args[0].toLowerCase())) {
            return sendErrorMessage(context, context.i18n("alreadyExists", args[0]));
        }

        if (transformer.getAliases().size() >= transformer.getType().getLimits().getAliases()) {
            context.makeWarning(context.i18n("noSlotsLeft")).queue();
            return false;
        }

        String alias = args[0].toLowerCase();
        String[] split = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).split(" ");
        CommandContainer command = CommandHandler.getCommand(context.getMessage(), split[0]);
        if (command == null) {
            return sendErrorMessage(context, context.i18n("invalidCommand", split[0]));
        }

        String commandString = command.getDefaultPrefix()
            + command.getCommand().getTriggers().get(0) + " "
            + String.join(" ", Arrays.copyOfRange(split, 1, split.length));

        transformer.getAliases().put(alias, commandString);

        try {
            updateGuildAliases(context, transformer);

            context.makeSuccess(context.i18n("created"))
                .set("alias", args[0])
                .set("command", commandString)
                .set("slots", transformer.getType().getLimits().getAliases() - transformer.getAliases().size())
                .queue();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean removeCustomAlias(CommandMessage context, GuildTransformer transformer, String[] args) {
        if (!transformer.getAliases().containsKey(args[0].toLowerCase())) {
            return sendErrorMessage(context, context.i18n("invalidAlias", args[0]));
        }

        transformer.getAliases().remove(args[0].toLowerCase());

        try {
            updateGuildAliases(context, transformer);

            context.makeSuccess(context.i18n("deleted"))
                .set("alias", args[0])
                .queue();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateGuildAliases(CommandMessage message, GuildTransformer transformer) throws SQLException {
        avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
            .where("id", message.getGuild().getId())
            .update(statement -> statement.set("aliases", AvaIre.gson.toJson(transformer.getAliases()), true));
    }
}

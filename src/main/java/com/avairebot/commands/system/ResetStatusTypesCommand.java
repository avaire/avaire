/*
 * Copyright (c) 2019.
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

package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.SystemCommand;
import com.avairebot.database.migrate.migrations.CreateGuildTypeTableMigration;
import com.avairebot.utilities.RandomUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ResetStatusTypesCommand extends SystemCommand {

    public static final Cache<String, String> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(60, TimeUnit.SECONDS)
        .build();

    private static final Logger log = LoggerFactory.getLogger(ResetStatusTypesCommand.class);

    public ResetStatusTypesCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Reset Status Types Command";
    }

    @Override
    public String getDescription() {
        return "Resets the status types in the database, this is useful overwriting the server status types limits to get new features after updating the bot, or to simply reset back to the default if it breaks after editing it with custom changes.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <game>` - Resets the server status types."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command`");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(SetStatusCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("reset-status");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        String cacheKey = context.getGuild().getId() + ":" + context.getAuthor().getId();
        String token = cache.getIfPresent(cacheKey);

        if (token == null) {
            token = RandomUtil.generateString(RandomUtil.getInteger(3) + 4);

            context.makeWarning(String.join("\n", Arrays.asList(
                "You're about to reset the server status types, this will",
                "reset everything back to the default and remove any custom",
                "changes you might've made to the status types.",
                "",
                "**This change is permanent!**",
                "",
                "If you're sure you wanna continue, run the command to completely",
                "reset the server status types.```:command :token```",
                "This command will only work for 60 seconds, after that time the token",
                "becomes invalid and the command has to be ran again to get a new token."
            )))
                .setTitle("Warning!")
                .set("command", generateCommandTrigger(context.getMessage()))
                .set("token", token)
                .queue();

            cache.put(cacheKey, token);

            return false;
        }

        if (args.length == 0 || !args[0].equals(token)) {
            return sendErrorMessage(context, "Invalid security token given, please provide the correct security token to reset server status types.");
        }

        try {
            avaire.getDatabase().getMigrations()
                .rerun(new CreateGuildTypeTableMigration());

            context.makeSuccess("The server status types have been reset successfully!").queue();
        } catch (SQLException e) {
            log.error("Failed to reset the server status types migration, error: {}", e.getMessage(), e);

            context.makeError("Something went wrong while re-running the database migration, please check the console for more information.");
        }

        return true;
    }
}

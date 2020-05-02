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
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.controllers.EconomyController;
import com.avairebot.database.transformers.EconomyTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.MentionableUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.entities.User;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BalanceCommand extends Command
{

    public static final Cache<Long, Collection> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(120, TimeUnit.SECONDS)
        .build();


    public BalanceCommand(AvaIre avaire) {
        super(avaire);
    }

    /**
     * Gets the command name, this is used in help and error
     * messages as the title as well as log messages.
     *
     * @return Never-null, the command name.
     */
    @Override
    public String getName() {
        return "Balance Command";
    }

    @Override
    public String getDescription()
    {
        return "Returns user balance.";
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
        return Collections.singletonList("balance");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:channel,1,5");
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
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        User user = context.getAuthor();
        if (args.length > 0 && !args[0].equals("---skip-mentions")) {
            user = MentionableUtil.getUser(context, new String[]{String.join(" ", args)});
            if (user == null) {
                user = context.getAuthor();
            }
        }
        final User author = user;

        if (author.isBot()) {
            context.makeWarning(context.i18n("botsCannotReceiveCurrency")).queue();
            return false;
        }
        EconomyTransformer economyTransformer = EconomyController.fetchEconomy(avaire,context.getMessage(),context.getAuthor().getIdLong());

        if(economyTransformer == null)
        {
            return sendErrorMessage(context,"Cannot load player balance");
        }
        long economy = economyTransformer.getBalance();
        context.makeSuccess("Balance : " + economy).queue();

        return true;
    }
}

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
import com.avairebot.database.controllers.EconomyController;
import com.avairebot.database.transformers.EconomyTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.time.Carbon;

import java.util.Collections;
import java.util.List;

public class DailyCommand extends Command
{
    private int amountToIncreaseBalanceBy = 50;

    public DailyCommand(AvaIre avaire)
    {
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
        return "Daily Command";
    }

    @Override
    public String getDescription()
    {
        return "Allows users to claim an amount of currency daily.";
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
        return Collections.singletonList("daily");
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


        EconomyTransformer economy = EconomyController.fetchEconomy(avaire,context.getMessage(),context.getAuthor().getIdLong());
        Carbon expire = avaire.getVoteManager().getExpireTime(context.getAuthor());

        int timesClaimed = economy.getDailyClaims();
        if(timesClaimed == 0)
        {
            economy.incrementBalanceBy(amountToIncreaseBalanceBy);
            EconomyController.updateUserClaims(avaire,context.getGuild().getIdLong(),context.getAuthor().getIdLong(),timesClaimed + 1);

            context.makeSuccess("Increased balance by " + amountToIncreaseBalanceBy).queue();
            return true;
        }
        else if(timesClaimed == 1)
        {
            if (expire != null && expire.isFuture())
            {
                economy.incrementBalanceBy(amountToIncreaseBalanceBy);
                EconomyController.updateUserClaims(avaire,context.getGuild().getIdLong(),context.getAuthor().getIdLong(),timesClaimed + 1);
                context.makeSuccess("Increased balance by " + amountToIncreaseBalanceBy).queue();
            }
            else
            {
                context.makeError(context.i18n("needToVoteAgain"));
                return true;
            }
        }
        else
        {
            context.makeError(context.i18n("alreadyClaimed"));
        }

        return false;
    }
}

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

package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Request;
import com.avairebot.requests.Response;
import com.avairebot.requests.service.HolidayService;
import com.avairebot.utilities.ColorUtil;
import com.avairebot.utilities.RestActionUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class HolidayCommand extends Command
{

    private static Cache<String, List<HolidayService.Holiday>> holidayMap = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build();

    public HolidayCommand(AvaIre avaire)
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
    public String getName()
    {
        return "HolidayCommand";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Gets the current holidays for today's date and posts the current holiday information.");
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
        return Collections.singletonList("holiday");
    }

    /**
     * Gets the command description, this is used in help messages to help
     * users get a better understanding of what the command does.
     *
     * @return Never-null, the command description.
     */
    @Override
    public String getDescription()
    {
        return "Retrieves whether or not today is a holiday.";
    }

    /**
     * Gets an immutable list of middlewares that should be added to the command stack
     * before the command is executed, if the middleware that intercepts the
     * command message event fails the command will never be executed.
     *
     * @return An immutable list of command middlewares that should be invoked before the command.
     * @see Middleware
     */
    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,1,2");
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
        Request request = RequestFactory.makeGET("https://holidayapi.com/v1/holidays");
        LocalDate date = LocalDate.now();
        int dayOfMonth = date.getDayOfMonth();
        int monthOfYear = date.getMonthValue();
        String holidayApiClientToken = avaire.getConfig().getString("apiKeys.holidayapi", "invalid");
        if (holidayApiClientToken.equals("invalid") || holidayApiClientToken.length() != 32)
        {

            return false;
        }

        request.addParameter("key", holidayApiClientToken);
        request.addParameter("year",date.getYear() - 1);
        request.addParameter("day",dayOfMonth);
        request.addParameter("month",monthOfYear);
        request.addParameter("country","US");
        String key = monthOfYear + "/" + dayOfMonth;
        if(holidayMap.getIfPresent(key) != null)
        {
            List<HolidayService.Holiday> holidays = holidayMap.getIfPresent(key);
            sendHolidayStatus(context,holidays);
        }
        else
        {
            request.send((Consumer<Response>) response ->
            {
                int statusCode = response.getResponse().code();

                if (statusCode == 429) {
                    context.makeWarning(context.i18n("tooManyAttempts"))
                        .queue(message -> message.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore));

                    return;
                }

                if (statusCode == 404) {
                    context.makeWarning(context.i18n("notFound"))
                        .queue(message -> message.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore));

                    return;
                }

                if (statusCode == 200)
                {
                    HolidayService service = (HolidayService) response.toService(HolidayService.class);
                    List<HolidayService.Holiday> holidays = service.getHolidays();
                    holidayMap.put(key,holidays);
                    sendHolidayStatus(context,holidays);
                    return;
                }

                context.makeError(context.i18n("somethingWentWrong")).queue();
            });
        }


        return true;
    }




    private void sendHolidayStatus(CommandMessage context,List<HolidayService.Holiday> holidays)
    {
        if(holidays == null || holidays.isEmpty())
        {
            context.makeEmbeddedMessage(ColorUtil.getColorFromString("0x2A2C31"),context.i18n("noHolidaysToday"))
                .queue();
        }
        else
        {
            context.makeEmbeddedMessage(ColorUtil.getColorFromString("0x2A2C31"),context.i18n("todayHoliday",holidays.get(0).getName()))
                .queue();
        }

    }


}

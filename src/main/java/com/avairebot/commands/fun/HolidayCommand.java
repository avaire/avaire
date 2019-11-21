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
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.Command;
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

public class HolidayCommand extends Command {

    private static Cache<String, List<HolidayService.Holiday>> cache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build();

    public HolidayCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Holiday Command";
    }

    @Override
    public String getDescription() {
        return "Retrieves whether or not today is a holiday, and if there is a holiday today, what it is what information about it.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command` - Gets the current holidays for today's date and posts the current holiday information."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("holiday");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:channel,1,3");
    }

    @Override
    public CommandPriority getCommandPriority() {
        if (getValidAPIToken() == null) {
            return CommandPriority.HIDDEN;
        }

        return super.getCommandPriority();
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (getValidAPIToken() == null) {
            return false;
        }

        LocalDate date = LocalDate.now();
        String key = date.getMonthValue() + "/" + date.getDayOfMonth();

        return cache.getIfPresent(key) != null
            ? sendHolidayStatus(context, cache.getIfPresent(key))
            : sendRequest(context, date, key);
    }

    private boolean sendRequest(CommandMessage context, LocalDate date, String key) {
        Request request = RequestFactory.makeGET("https://holidayapi.com/v1/holidays");

        request.addParameter("key", getValidAPIToken());
        request.addParameter("year", date.getYear() - 1);
        request.addParameter("day", date.getDayOfMonth());
        request.addParameter("month", date.getMonthValue());
        request.addParameter("country", "US");

        request.send((Consumer<Response>) response -> {
            switch (response.getResponse().code()) {
                case 429:
                    context.makeWarning(context.i18n("tooManyAttempts"))
                        .queue(message -> message.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore));
                    break;

                case 404:
                    context.makeWarning(context.i18n("notFound"))
                        .queue(message -> message.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore));
                    break;

                case 200:
                    HolidayService service = (HolidayService) response.toService(HolidayService.class);
                    List<HolidayService.Holiday> holidays = service.getHolidays();

                    cache.put(key, holidays);
                    sendHolidayStatus(context, holidays);
                    break;

                default:
                    context.makeError(context.i18n("somethingWentWrong")).queue();
            }
        });

        return true;
    }

    private boolean sendHolidayStatus(CommandMessage context, List<HolidayService.Holiday> holidays) {
        if (holidays == null || holidays.isEmpty()) {
            context.makeEmbeddedMessage(
                ColorUtil.getColorFromString("0x2A2C31"),
                context.i18n("noHolidaysToday")
            ).queue();

            return true;
        }

        context.makeEmbeddedMessage(
            ColorUtil.getColorFromString("0x2A2C31"),
            context.i18n("todayHoliday", holidays.get(0).getName())
        ).queue();

        return true;
    }

    private String getValidAPIToken() {
        String holidayApiClientToken = avaire.getConfig().getString("apiKeys.holidayapi", "invalid");
        if (holidayApiClientToken.equals("invalid") || holidayApiClientToken.length() != 36) {
            return null;
        }

        return holidayApiClientToken;
    }
}

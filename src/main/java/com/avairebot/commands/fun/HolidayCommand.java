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

    private static Cache<String, List<HolidayService.Holiday>> holidayMap = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build();

    public HolidayCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "HolidayCommand";
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
    public String getDescription() {
        return "Retrieves whether or not today is a holiday.";
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,1,2");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        Request request = RequestFactory.makeGET("https://holidayapi.com/v1/holidays");
        LocalDate date = LocalDate.now();
        int dayOfMonth = date.getDayOfMonth();
        int monthOfYear = date.getMonthValue();
        String holidayApiClientToken = avaire.getConfig().getString("apiKeys.holidayapi", "invalid");
        if (holidayApiClientToken.equals("invalid") || holidayApiClientToken.length() < 32) {
            return false;
        }

        request.addParameter("key", holidayApiClientToken);
        request.addParameter("year", date.getYear() - 1);
        request.addParameter("day", dayOfMonth);
        request.addParameter("month", monthOfYear);
        request.addParameter("country", "US");
        String key = monthOfYear + "/" + dayOfMonth;
        if (holidayMap.getIfPresent(key) != null) {
            List<HolidayService.Holiday> holidays = holidayMap.getIfPresent(key);
            sendHolidayStatus(context, holidays);
        } else {
            sendRequest(context, request, key);
        }

        return true;
    }

    private void sendRequest(CommandMessage context, Request request, String key) {
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

            if (statusCode == 200) {
                HolidayService service = (HolidayService) response.toService(HolidayService.class);
                List<HolidayService.Holiday> holidays = service.getHolidays();
                holidayMap.put(key, holidays);
                sendHolidayStatus(context, holidays);
                return;
            }

            context.makeError(context.i18n("somethingWentWrong")).queue();
        });
    }

    private void sendHolidayStatus(CommandMessage context, List<HolidayService.Holiday> holidays) {
        if (holidays == null || holidays.isEmpty()) {
            context.makeEmbeddedMessage(ColorUtil.getColorFromString("0x2A2C31"), context.i18n("noHolidaysToday"))
                .queue();
        } else {
            context.makeEmbeddedMessage(ColorUtil.getColorFromString("0x2A2C31"), context.i18n("todayHoliday", holidays.get(0).getName()))
                .queue();
        }
    }
}

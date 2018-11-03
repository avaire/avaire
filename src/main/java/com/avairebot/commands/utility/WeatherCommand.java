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

package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.scheduler.tasks.DrainWeatherQueueTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WeatherCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(WeatherCommand.class);

    public WeatherCommand(AvaIre avaire) {
        super(avaire);

        if (!hasApiKey()) {
            log.debug("No valid API key given, disabling weather command");
        }
    }

    @Override
    public String getName() {
        return "Weather Command";
    }

    @Override
    public String getDescription() {
        return "Gets the current weather and wind information for the given location, showing the temperature in both celsius and fahrenheit, as well as showing the wind speed in kilometers and miles per hour, and what direction the wind is blowing in.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <city>` - Gets the weather for the city.",
            "`:command <city>, <country code>` - Gets the weather for the city in the given country."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command London, UK` - Gets the Weather from London in the UK.");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("weather");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,1,5");
    }

    @Override
    public CommandPriority getCommandPriority() {
        return hasApiKey() ? super.getCommandPriority() : CommandPriority.HIDDEN;
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.INFORMATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (!hasApiKey()) {
            return false;
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "city");
        }

        DrainWeatherQueueTask.queueWeather(new DrainWeatherQueueTask.WeatherEntity(
            context.getAuthor().getIdLong(),
            context.getMessageChannel().getIdLong(),
            String.join(" ", args)
        ));

        context.getMessageChannel().sendTyping().queue();

        return true;
    }

    private boolean hasApiKey() {
        return avaire.getConfig().getString("apiKeys.openWeatherMap", "").length() == 32;
    }
}

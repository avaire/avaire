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

package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.NumberUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TemperatureConvertCommand extends Command {

    public TemperatureConvertCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Temperature Convert Command";
    }

    @Override
    public String getDescription() {
        return "Converts the given temperature to Celsius or vice-versa";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <temperature> <tempOutputFormat>` - Converts the given temperature to a different format.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList("`:command 50 f` - Converts the given temperature to Celsius. ", "`:command 40 c` - Converts the given temperature to Fahrenheit.");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("tempconvert");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "temperature", "tempOutputFormat");
        } else if (args.length == 1) {
            return sendErrorMessage(context, "errors.missingArgument", "tempOutputFormat");
        }

        try {
            double temperature = Double.parseDouble(args[0]);
            char tempMode = args[1].toUpperCase().charAt(0);

            switch (tempMode) {
                case 'C':
                    double fahrenheit = celsiusToFahrenheit(temperature);
                    context.makeInfo(NumberUtil.formatNicelyWithDecimals(fahrenheit) + "° F").queue();
                    break;

                case 'F':
                    double celsius = fahrenheitToCelsius(temperature);
                    context.makeInfo(NumberUtil.formatNicelyWithDecimals(celsius) + "° C").queue();
                    break;

                default:
                    return sendErrorMessage(context, context.i18n("invalidConvertFormat", args[1], String.join("\n ,", getValidConvertModes())));
            }
        } catch (NumberFormatException ex) {
            return sendErrorMessage(context, context.i18n("invalidNumberFormat", args[0]));
        }

        return true;
    }

    private double celsiusToFahrenheit(double celsius) {
        return ((celsius * 9) / 5) + 32;
    }

    private double fahrenheitToCelsius(double fahrenheit) {
        return ((fahrenheit - 32) * 5) / 9;
    }

    private List<String> getValidConvertModes() {
        return Arrays.asList(
            "f : Fahrenheit to Celsius",
            "c : Celsius to Fahrenheit"
        );
    }
}

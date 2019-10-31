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
        return "Converts the given temperature to Celsius, Fahrenheit, and/or Kelvin.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <temperature>` - Converts the temperature to a different format.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command 50c` - Converts from celsius to fahrenheit and kelvin. ");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("tempconvert", "temperature", "temp");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0 || args[0].length() == 1) {
            return sendErrorMessage(context, "errors.missingArgument", "temperature");
        }

        final String message = String.join(" ", args).trim();

        try {
            char temperatureConversion = message.toUpperCase().charAt(message.length() - 1);
            double temperature = Double.parseDouble(
                message.substring(0, message.length() - 1)
            );

            switch (temperatureConversion) {
                case 'C':
                    context.makeInfo(context.i18n("message"))
                        .set("input", NumberUtil.formatNicelyWithDecimals(temperature) + "° C")
                        .set("one", NumberUtil.formatNicelyWithDecimals(celsiusToFahrenheit(temperature)) + "° F")
                        .set("two", NumberUtil.formatNicelyWithDecimals(celsiusToKelvin(temperature)) + "° K")
                        .queue();
                    break;

                case 'F':
                    double fahrenheitToCelsius = fahrenheitToCelsius(temperature);

                    context.makeInfo(context.i18n("message"))
                        .set("input", NumberUtil.formatNicelyWithDecimals(temperature) + "° F")
                        .set("one", NumberUtil.formatNicelyWithDecimals(fahrenheitToCelsius) + "° C")
                        .set("two", NumberUtil.formatNicelyWithDecimals(celsiusToKelvin(fahrenheitToCelsius)) + "° K")
                        .queue();
                    break;

                case 'K':
                    double kelvinToCelsius = kelvinToCelsius(temperature);

                    context.makeInfo(context.i18n("message"))
                        .set("input", NumberUtil.formatNicelyWithDecimals(temperature) + "° K")
                        .set("one", NumberUtil.formatNicelyWithDecimals(kelvinToCelsius) + "° C")
                        .set("two", NumberUtil.formatNicelyWithDecimals(celsiusToFahrenheit(kelvinToCelsius)) + "° F")
                        .queue();
                    break;

                default:
                    return sendErrorMessage(
                        context,
                        context.i18n("invalidConvertFormat", temperatureConversion, String.join("\n", getValidConvertModes())));
            }
        } catch (NumberFormatException ex) {
            return sendErrorMessage(context, context.i18n("invalidNumberFormat", message.substring(
                0, message.length() - 1)
            ));
        }

        return true;
    }

    private double celsiusToKelvin(double celsius) {
        return celsius + 273.15D;
    }

    private double kelvinToCelsius(double kelvin) {
        return kelvin - 273.15D;
    }

    private double celsiusToFahrenheit(double celsius) {
        return ((celsius * 9) / 5) + 32;
    }

    private double fahrenheitToCelsius(double fahrenheit) {
        return ((fahrenheit - 32) * 5) / 9;
    }

    private List<String> getValidConvertModes() {
        return Arrays.asList(
            " - F : Fahrenheit to Celsius & Kelvin",
            " - C : Celsius to Fahrenheit & Kelvin",
            " - K : Kelvin to Celsius and Fahrenheit"
        );
    }
}

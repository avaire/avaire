/*
 * Copyright (c) 2022.
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

import com.avairebot.BaseTest;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTempConvertCommand extends BaseTest
{

    @Test
    public void CanConvert32FahrenheitTo0Celsius() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        TemperatureConvertCommand tempConvertCommand = new TemperatureConvertCommand(null);
        Method method = tempConvertCommand.getClass().getDeclaredMethod("fahrenheitToCelsius", double.class);
        method.setAccessible(true);
        double newTemperature = (double) method.invoke(tempConvertCommand, 32);
        assertEquals(0, newTemperature, "Command: TemperatureConvert did not correctly convert 32 degrees Fahrenheit to 0 degrees Celsius.");
    }

    @Test
    public void CanConvert0CelsiusTo273Kelvin() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        TemperatureConvertCommand tempConvertCommand = new TemperatureConvertCommand(null);
        Method method = tempConvertCommand.getClass().getDeclaredMethod("celsiusToKelvin", double.class);
        method.setAccessible(true);
        double newTemperature = (double) method.invoke(tempConvertCommand, 0);
        assertEquals(273.15, newTemperature, "Command: TemperatureConvert did not correctly convert 0 degrees Celsius to 273.15 degrees Kelvin.");
    }

    @Test
    public void CanConvert273KelvinTo0Fahrenheit() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        TemperatureConvertCommand tempConvertCommand = new TemperatureConvertCommand(null);
        Method method = tempConvertCommand.getClass().getDeclaredMethod("kelvinToCelsius", double.class);
        method.setAccessible(true);
        double newTemperature = (double) method.invoke(tempConvertCommand, 273.15);
        assertEquals(0, newTemperature, "Command: TemperatureConvert did not correctly convert 273.15 degrees Kelvin to 0 degrees Celsius.");
    }

    @Test
    public void CanConvert98FahrenheitTo37Celsius() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        TemperatureConvertCommand tempConvertCommand = new TemperatureConvertCommand(null);
        Method method = tempConvertCommand.getClass().getDeclaredMethod("fahrenheitToCelsius", double.class);
        method.setAccessible(true);
        double newTemperature = (double) method.invoke(tempConvertCommand, 98.6);
        assertEquals(37, newTemperature, "Command: TemperatureConvert did not correctly convert 98.6 degrees Fahrenheit to 37 degrees Celsius.");
    }

    @Test
    public void CanConvert37CelsiusTo98Fahrenheit() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        TemperatureConvertCommand tempConvertCommand = new TemperatureConvertCommand(null);
        Method method = tempConvertCommand.getClass().getDeclaredMethod("celsiusToFahrenheit", double.class);
        method.setAccessible(true);
        double newTemperature = (double) method.invoke(tempConvertCommand, 37);
        assertEquals(98.6, newTemperature, "Command: TemperatureConvert did not correctly convert 37 degrees Celsius to 98.6 degrees Fahrenheit.");
    }

    @Test
    public void CanConvert212FahrenheitTo100Celsius() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        TemperatureConvertCommand tempConvertCommand = new TemperatureConvertCommand(null);
        Method method = tempConvertCommand.getClass().getDeclaredMethod("fahrenheitToCelsius", double.class);
        method.setAccessible(true);
        double newTemperature = (double) method.invoke(tempConvertCommand, 212);
        assertEquals(100, newTemperature, "Command: TemperatureConvert did not correctly convert 212 degrees Fahrenheit to 100 degrees Celsius.");
    }

    @Test
    public void CanConvert100CelsiusTo212Fahrenheit() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        TemperatureConvertCommand tempConvertCommand = new TemperatureConvertCommand(null);
        Method method = tempConvertCommand.getClass().getDeclaredMethod("celsiusToFahrenheit", double.class);
        method.setAccessible(true);
        double newTemperature = (double) method.invoke(tempConvertCommand, 100);
        assertEquals(212, newTemperature, "Command: TemperatureConvert did not correctly convert 100 degrees Fahrenheit to 212 degrees Celsius.");
    }
}

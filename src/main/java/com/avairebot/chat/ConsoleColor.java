package com.avairebot.chat;

import com.avairebot.Settings;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;

public enum ConsoleColor {

    RESET("\u001B[0m", "reset"),
    BLACK("\u001B[30m", "black"),
    BLACK_BACKGROUND("\u001B[40m", "blackBg"),
    RED("\u001B[31m", "red"),
    RED_BACKGROUND("\u001B[41m", "redBg"),
    GREEN("\u001B[32m", "green"),
    GREEN_BACKGROUND("\u001B[42m", "greenBg"),
    YELLOW("\u001B[33m", "yellow"),
    YELLOW_BACKGROUND("\u001B[43m", "yellowBg"),
    BLUE("\u001B[34m", "blue"),
    BLUE_BACKGROUND("\u001B[44m", "blueBg"),
    PURPLE("\u001B[35m", "purple"),
    PURPLE_BACKGROUND("\u001B[45m", "purpleBg"),
    CYAN("\u001B[36m", "cyan"),
    CYAN_BACKGROUND("\u001B[46m", "cyanBg"),
    WHITE("\u001B[37m", "white"),
    WHITE_BACKGROUND("\u001B[47m", "whiteBg");

    private static Settings settings;

    private final String color;
    private final String format;

    ConsoleColor(String color, String format) {
        this.color = color;
        this.format = format;
    }

    public static String format(String string) {
        if (string == null) {
            return null;
        }

        boolean useColors = settings == null || settings.useColors();

        for (ConsoleColor color : values()) {
            string = string.replaceAll(
                Matcher.quoteReplacement("%" + color.getFormat()), useColors ? color.getColor() : ""
            );
        }
        return string;
    }

    public static void setSettings(@Nonnull Settings settings) {
        ConsoleColor.settings = settings;
    }

    public String getColor() {
        return color;
    }

    public String getFormat() {
        return format;
    }

    @Override
    public String toString() {
        return getColor();
    }
}

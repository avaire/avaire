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

package com.avairebot;

import ch.qos.logback.classic.util.ContextInitializer;
import com.avairebot.chat.ConsoleColor;
import com.avairebot.exceptions.InvalidApplicationEnvironmentException;
import com.avairebot.shared.ExitCodes;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws IOException, SQLException, InvalidApplicationEnvironmentException {
        Options options = new Options();

        options.addOption(new Option("h", "help", false, "Displays this help menu."));
        options.addOption(new Option("v", "version", false, "Displays the current version of the application."));
        options.addOption(new Option("sc", "shard-count", true, "Sets the amount of shards the bot should start up."));
        options.addOption(new Option("m", "music", false, "Enables music-only mode, disabling any feature that is not related to the music features."));
        options.addOption(new Option("env", "use-environment-variables", false, "Enables environment variables override for the config options, this allows for setting up environment variables like \"AVA_DISCORD_TOKEN\" to override the \"discord.token\" option in the config. Every option in the config can be overwritten with an environment variable called \"AVA_\" plus the path to the config option in all uppercase, and any special characters replaced with an underscore(_), for example \"database.type\" would be \"AVA_DATABASE_TYPE\".\nNote: None of the values are stored in the config permanently, removing the environment variable will make the bot use the config option again(after a restart)."));
        options.addOption(new Option("nocolor", "no-colors", false, "Disables colors for commands and AI actions in the terminal."));
        options.addOption(new Option("d", "debug", false, "Enables debugging mode, this will log extra information to the terminal."));

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            Settings settings = new Settings(cmd, args);
            ConsoleColor.setSettings(settings);
            if (!settings.useColors()) {
                System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "logback_nocolor" + (
                    settings.useDebugging() ? "_debug" : ""
                ) + ".xml");
            } else if (settings.useDebugging()) {
                System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "logback_debug.xml");
            }

            if (cmd.hasOption("help")) {
                formatter.printHelp("Help Menu", options);
                System.exit(ExitCodes.EXIT_CODE_NORMAL);
            } else if (cmd.hasOption("version")) {
                System.out.println(AvaIre.getVersionInfo(settings));
                System.exit(ExitCodes.EXIT_CODE_NORMAL);
            }

            AvaIre.avaire = new AvaIre(settings);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("", options);

            System.exit(ExitCodes.EXIT_CODE_NORMAL);
        }
    }
}

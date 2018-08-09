package com.avairebot;

import ch.qos.logback.classic.util.ContextInitializer;
import com.avairebot.chat.ConsoleColor;
import com.avairebot.commands.utility.TestCommand;
import com.avairebot.exceptions.InvalidApplicationEnvironmentException;
import com.avairebot.shared.ExitCodes;
import org.apache.commons.cli.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws IOException, SQLException, InvalidApplicationEnvironmentException {
        Options options = new Options();

        options.addOption(new Option("h", "help", false, "Displays this help menu."));
        options.addOption(new Option("v", "version", false, "Displays the current version of the application."));
        options.addOption(new Option("sc", "shard-count", true, "Sets the amount of shards the bot should start up."));
        options.addOption(new Option("is", "internal-restart", false, "Enables internal restarts, this will auto restart the bot if it crashes, or the system restart or update commands are used, the only way to really shut down the bot is through the system shutdown command.\nNote: The update command will just act as the restart command with this option enabled."));
        options.addOption(new Option("nocolor", "no-colors", false, "Disables colors for commands and AI actions in the terminal."));
        options.addOption(new Option("d", "debug", false, "Enables debugging mode, this will log extra information to the terminal."));
        options.addOption(new Option("t", "test", false, "Runs the test code."));

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

            if (cmd.hasOption("test")) {
                try {
                    BufferedImage bufferedImage = TestCommand.generateImage(
                        "https://cdn.discordapp.com/avatars/88739639380172800/e2b182faea7cebeca33cc7ed34c26feb.png",
                        "Senither",
                        "0001",
                        "27",
                        "99",
                        "9,001",
                        "9,876",
                        "17,679",
                        "397,421",
                        84.8D
                    );
                    ImageIO.write(bufferedImage, "png", new File("test-output.png"));
                } catch (FontFormatException e) {
                    e.printStackTrace();
                }
                System.exit(ExitCodes.EXIT_CODE_NORMAL);
            }

            if (cmd.hasOption("help")) {
                formatter.printHelp("Help Menu", options);
                System.exit(ExitCodes.EXIT_CODE_NORMAL);
            } else if (cmd.hasOption("version")) {
                System.out.println(AvaIre.getVersionInfo());
                System.exit(ExitCodes.EXIT_CODE_NORMAL);
            }

            new AvaIre(settings);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("", options);

            System.exit(ExitCodes.EXIT_CODE_NORMAL);
        }
    }
}

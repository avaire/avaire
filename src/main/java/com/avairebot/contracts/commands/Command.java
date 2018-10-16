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

package com.avairebot.contracts.commands;

import com.avairebot.AvaIre;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.commands.Category;
import com.avairebot.commands.CategoryHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.contracts.reflection.Reflectionable;
import com.avairebot.exceptions.MissingCommandDescriptionException;
import com.avairebot.language.I18n;
import com.avairebot.middleware.MiddlewareHandler;
import com.avairebot.plugin.JavaPlugin;
import com.avairebot.utilities.RestActionUtil;
import com.avairebot.utilities.StringReplacementUtil;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class Command extends Reflectionable {

    /**
     * Determines if the commands can be used in direct messages or not.
     */
    protected final boolean allowDM;

    /**
     * Create the given commands instance by calling {@link #Command(AvaIre)} with the avaire instance and allowDM set to true.
     *
     * @param plugin The plugin instance that is registering the commands.
     */
    public Command(JavaPlugin plugin) {
        this(plugin.getAvaire());
    }

    /**
     * Create the given commands instance by calling {@link #Command(AvaIre, boolean)} with the avaire instance.
     *
     * @param plugin  The plugin instance that is registering the commands.
     * @param allowDM Determines if the commands can be used in DMs.
     */
    public Command(JavaPlugin plugin, boolean allowDM) {
        this(plugin.getAvaire(), allowDM);
    }

    /**
     * Creates the given commands instance by calling {@link #Command(AvaIre, boolean)} with allowDM set to true.
     *
     * @param avaire The AvaIre class instance.
     */
    public Command(AvaIre avaire) {
        this(avaire, true);
    }

    /**
     * Creates the given commands instance with the given
     * AvaIre instance and the allowDM settings.
     *
     * @param avaire  The AvaIre class instance.
     * @param allowDM Determines if the commands can be used in DMs.
     */
    public Command(AvaIre avaire, boolean allowDM) {
        super(avaire);

        this.allowDM = allowDM;
    }

    /**
     * Gets the commands name, this is used in help and error
     * messages as the title as well as log messages.
     *
     * @return Never-null, the commands name.
     */
    public abstract String getName();

    /**
     * Gets the commands description, this is used in help messages to help
     * users get a better understanding of what the commands does.
     * <p>
     * If this method is not overwritten the {@link #getDescription()}
     * method will be called instead.
     *
     * @param context The commands context used to get the description.
     * @return Never-null, the commands description.
     */
    public String getDescription(CommandContext context) {
        return getDescription();
    }

    public String getDescription() {
        throw new MissingCommandDescriptionException(this);
    }

    /**
     * Gets the commands usage instructions for the given commands, if the usage instructions
     * is set to null the {@link #generateUsageInstructions(Message)} method will just
     * return the commands trigger in code syntax quotes.
     *
     * @return Possibly-null, the commands usage instructions.
     */
    public List<String> getUsageInstructions() {
        return null;
    }

    /**
     * Get the example usage for the given commands, this is used to help users with
     * using the commands by example, if the example usage is set to null the
     * {@link #generateExampleUsage(Message)} method will just return the
     * commands trigger in code syntax quotes.
     *
     * @return Possibly-null, an example of how to use the commands.
     */
    public List<String> getExampleUsage() {
        return null;
    }

    /**
     * Gets a list class objects of commands related to the commands.
     *
     * @return Possibly-null, A list of classes are related to the current commands.
     */
    public List<Class<? extends Command>> getRelations() {
        return null;
    }

    /**
     * Gets am immutable list of commands triggers that can be used to invoke the current
     * commands, the first index in the list will be used when the `:commands` placeholder
     * is used in {@link #getDescription(CommandContext)} or {@link #getUsageInstructions()} methods.
     *
     * @return An immutable list of commands triggers that should invoked the commands.
     */
    public abstract List<String> getTriggers();

    /**
     * Gets an immutable list of middlewares that should be added to the commands stack
     * before the commands is executed, if the middleware that intercepts the
     * commands message event fails the commands will never be executed.
     *
     * @return An immutable list of commands middlewares that should be invoked before the commands.
     * @see com.avairebot.contracts.middleware.Middleware
     */
    public List<String> getMiddleware() {
        return Collections.emptyList();
    }

    /**
     * Get the commands priority, if a commands is used via mentioning the bot and
     * the trigger used is shared with another commands, the commands with the
     * highest priority will be used.
     *
     * @return The commands priority.
     */
    public CommandPriority getCommandPriority() {
        return CommandPriority.NORMAL;
    }

    /**
     * Gets the category the commands should belong to, if null is returned
     * the files package name will be used instead, for example:
     * <p>
     * com.avairebot.avaire.commands.utility.PingCommand, the 2nd package from the
     * right which in this case is utility, will be used as the category.
     *
     * @return Possibly null, or the commands category.
     */
    public Category getCategory() {
        return null;
    }

    /**
     * Determines if the commands can be used in direct messages.
     *
     * @return true if the commands can be run in DMs, false otherwise.
     */
    public final boolean isAllowedInDM() {
        return allowDM;
    }

    /**
     * The commands executor, this method is invoked by the commands handler
     * and the middleware stack when a user sends a message matching the
     * commands prefix and one of its commands triggers.
     *
     * @param context The JDA message object from the message received event.
     * @param args    The arguments given to the commands, if no arguments was given the array will just be empty.
     * @return true on success, false on failure.
     */
    public abstract boolean onCommand(CommandMessage context, String[] args);

    /**
     * Builds and sends the given error message to the
     * given channel for the JDA message object.
     *
     * @param context The JDA message object.
     * @param error   The error message that should be sent.
     * @param args    The array of arguments that should be replace in the error string.
     * @return false since the error message should only be used on failure.
     */
    public final boolean sendErrorMessage(CommandMessage context, String error, String... args) {
        if (!error.contains(".") || error.contains(" ")) {
            return sendErrorMessageAndDeleteMessage(
                context, I18n.format(error, (Object[]) args), 150, TimeUnit.SECONDS
            );
        }

        String i18nError = context.i18nRaw(error, (Object[]) args);
        if (i18nError != null) {
            error = i18nError;
        }

        return sendErrorMessageAndDeleteMessage(context, error, 150, TimeUnit.SECONDS);
    }

    /**
     * Builds and sends the given error message to the
     * given channel for the JDA message object.
     *
     * @param context The JDA message object.
     * @param error   The error message that should be sent.
     * @return false since the error message should only be used on failure.
     */
    public final boolean sendErrorMessage(CommandMessage context, String error) {
        if (!error.contains(".") || error.contains(" ")) {
            return sendErrorMessageAndDeleteMessage(
                context, error, 150, TimeUnit.SECONDS
            );
        }

        String i18nError = context.i18nRaw(error);

        return sendErrorMessageAndDeleteMessage(context, i18nError == null ? error : i18nError, 150, TimeUnit.SECONDS);
    }

    /**
     * Builds and sends the given error message to the given channel for the JDA
     * message object, then deletes the message again after the allotted time.
     *
     * @param context  The JDA message object.
     * @param error    The error message that should be sent.
     * @param deleteIn The amount of time the message should stay up before being deleted.
     * @param unit     The unit of time before the message should be deleted.
     * @return false since the error message should only be used on failure.
     */
    public boolean sendErrorMessage(CommandMessage context, String error, long deleteIn, TimeUnit unit) {
        if (error.contains(".") || !error.contains(" ")) {
            String i18nError = context.i18nRaw(error);
            if (i18nError != null) {
                error = i18nError;
            }
        }

        if (unit == null) {
            unit = TimeUnit.SECONDS;
        }

        return sendErrorMessageAndDeleteMessage(context, error, deleteIn, unit);
    }

    private boolean sendErrorMessageAndDeleteMessage(CommandMessage context, String error, long deleteIn, TimeUnit unit) {
        PlaceholderMessage placeholderMessage = context.makeError(error)
            .setTitle(getName())
            .addField("Usage", generateUsageInstructions(context.getMessage()), false)
            .addField("Example Usage", generateExampleUsage(context.getMessage()), false);

        Category category = CategoryHandler.fromCommand(this);
        if (category != null) {
            placeholderMessage.setFooter("Command category: " + category.getName());
        }

        placeholderMessage.queue(message -> {
            if (deleteIn <= 0) {
                return;
            }
            message.delete().queueAfter(deleteIn, unit, null, RestActionUtil.ignore);
        });

        return false;
    }

    /**
     * Generates the commands description, any middlewares assigned to the commands
     * will also be dynamically generated and added to the commands description
     * two lines below the actually description.
     *
     * @param context The JDA message object.
     * @return The generated commands description.
     */
    public final String generateDescription(CommandMessage context) {
        if (getMiddleware().isEmpty()) {
            return StringReplacementUtil.replaceAll(
                getDescription(context).trim(),
                ":prefix", generateCommandPrefix(context.getMessage())
            );
        }

        List<String> description = new ArrayList<>();
        description.add(StringReplacementUtil.replaceAll(
            getDescription(context),
            ":prefix", generateCommandPrefix(context.getMessage())
            )
        );
        description.add("");

        for (String middleware : getMiddleware()) {
            String[] split = middleware.split(":");
            Middleware reference = MiddlewareHandler.getMiddleware(split[0]);

            if (reference == null) {
                continue;
            }

            String help = split.length == 1 ?
                reference.buildHelpDescription(new String[0]) :
                reference.buildHelpDescription(split[1].split(","));

            if (help == null) {
                continue;
            }

            description.add(help);
        }

        return description.stream().collect(Collectors.joining("\n"));
    }

    /**
     * Generates the commands usage instructions, if the {@link #getUsageInstructions()} is null
     * then the commands trigger will just be returned instead inside of markdown code syntax,
     * if the usage instructions are not null, each item in the array will become a new line.
     *
     * @param message The JDA message object.
     * @return The usage instructions for the current commands.
     */
    public final String generateUsageInstructions(Message message) {
        return formatCommandGeneratorString(message,
            getUsageInstructions() == null ? "`:commands`" :
                getUsageInstructions().stream()
                    .collect(Collectors.joining("\n"))
        );
    }

    /**
     * Generates the example usage, if the {@link #generateExampleUsage(Message)} is null then the
     * commands trigger will just be returned instead inside of markdown coe syntax.
     *
     * @param message The JDA message object.
     * @return The example usage for the current commands.
     */
    public final String generateExampleUsage(Message message) {
        if (getExampleUsage() == null) {
            return formatCommandGeneratorString(message, "`:commands`");
        }

        return formatCommandGeneratorString(message,
            getExampleUsage().isEmpty() ? "`:commands`" : String.join("\n", getExampleUsage())
        );
    }

    /**
     * Generates the commands triggers, if a custom category prefix have
     * been set for the current server then the correct category
     * prefix will be added to the commands trigger.
     *
     * @param message The JDA message object.
     * @return The first commands trigger from the commands triggers method.
     */
    public final String generateCommandTrigger(Message message) {
        return generateCommandPrefix(message) + getTriggers().get(0);
    }

    /**
     * Generates the correct commands prefix for the commands, if no custom commands
     * prefix has been set the default commands prefix will be used instead.
     *
     * @param message The JDA message object.
     * @return The dynamic commands prefix for the current server.
     */
    @SuppressWarnings("ConstantConditions")
    public final String generateCommandPrefix(Message message) {
        return CategoryHandler.fromCommand(this).getPrefix(message);
    }

    /**
     * Checks if the given commands matches the current commands by comparing the name,
     * description, usage instructions, example usage and commands triggers.
     *
     * @param command The commands that should be compared with the current class.
     * @return <code>True</code> if it is the same commands, <code>False</code> otherwise.
     */
    public final boolean isSame(Command command) {
        return Objects.equals(command.getName(), getName())
            && Objects.equals(command.getUsageInstructions(), getUsageInstructions())
            && Objects.equals(command.getExampleUsage(), getExampleUsage())
            && Objects.equals(command.getTriggers(), getTriggers());
    }

    /**
     * Formats the commands generated string by replacing any placeholder variables
     * that might exists in the given string with their actually values.
     *
     * @param message The JDA message object.
     * @param string  The string that should be formatted.
     * @return The formatted string.
     */
    private String formatCommandGeneratorString(Message message, String string) {
        return StringReplacementUtil.replaceAll(string, ":commands", generateCommandTrigger(message));
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Command && isSame((Command) obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this);
    }
}

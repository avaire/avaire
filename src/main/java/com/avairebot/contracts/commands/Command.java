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
import com.avairebot.language.I18n;
import com.avairebot.middleware.MiddlewareHandler;
import com.avairebot.plugin.JavaPlugin;
import com.avairebot.utilities.RestActionUtil;
import com.avairebot.utilities.StringReplacementUtil;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class Command extends Reflectionable {

    /**
     * Determines if the command can be used in direct messages or not.
     */
    protected final boolean allowDM;

    /**
     * Create the given command instance by calling {@link #Command(AvaIre)} with the avaire instance and allowDM set to true.
     *
     * @param plugin The plugin instance that is registering the command.
     */
    public Command(JavaPlugin plugin) {
        this(plugin.getAvaire());
    }

    /**
     * Create the given command instance by calling {@link #Command(AvaIre, boolean)} with the avaire instance.
     *
     * @param plugin  The plugin instance that is registering the command.
     * @param allowDM Determines if the command can be used in DMs.
     */
    public Command(JavaPlugin plugin, boolean allowDM) {
        this(plugin.getAvaire(), allowDM);
    }

    /**
     * Creates the given command instance by calling {@link #Command(AvaIre, boolean)} with allowDM set to true.
     *
     * @param avaire The AvaIre class instance.
     */
    public Command(AvaIre avaire) {
        this(avaire, true);
    }

    /**
     * Creates the given command instance with the given
     * AvaIre instance and the allowDM settings.
     *
     * @param avaire  The AvaIre class instance.
     * @param allowDM Determines if the command can be used in DMs.
     */
    public Command(AvaIre avaire, boolean allowDM) {
        super(avaire);

        this.allowDM = allowDM;
    }

    /**
     * Gets the command name, this is used in help and error
     * messages as the title as well as log messages.
     *
     * @return Never-null, the command name.
     */
    public abstract String getName();

    /**
     * Gets the command description, this is used in help messages to help
     * users get a better understanding of what the command does.
     * <p>
     * If this method is not overwritten the {@link #getDescription()}
     * method will be called instead.
     *
     * @param context The command message context generated using the
     *                JDA message event that invoked the command.
     * @return Never-null, the command description.
     */
    public String getDescription(@Nullable CommandContext context) {
        return getDescription();
    }

    /**
     * Gets the command description, this is used in help messages to help
     * users get a better understanding of what the command does.
     *
     * @return Never-null, the command description.
     */
    public String getDescription() {
        return getDescription(null);
    }

    /**
     * Gets the command usage instructions for the given command, if the usage instructions
     * is set to null the {@link #generateUsageInstructions(Message)} method will just
     * return the command trigger in code syntax quotes.
     *
     * @return Possibly-null, the command usage instructions.
     */
    public List<String> getUsageInstructions() {
        return null;
    }

    /**
     * Get the example usage for the given command, this is used to help users with
     * using the command by example, if the example usage is set to null the
     * {@link #generateExampleUsage(Message)} method will just return the
     * command trigger in code syntax quotes.
     *
     * @return Possibly-null, an example of how to use the command.
     */
    public List<String> getExampleUsage() {
        return null;
    }

    /**
     * Gets a list class objects of commands related to the command.
     *
     * @return Possibly-null, A list of classes are related to the current command.
     */
    public List<Class<? extends Command>> getRelations() {
        return null;
    }

    /**
     * Gets am immutable list of command triggers that can be used to invoke the current
     * command, the first index in the list will be used when the `:command` placeholder
     * is used in {@link #getDescription(CommandContext)} or {@link #getUsageInstructions()} methods.
     *
     * @return An immutable list of command triggers that should invoked the command.
     */
    public abstract List<String> getTriggers();

    /**
     * Gets an immutable list of middlewares that should be added to the command stack
     * before the command is executed, if the middleware that intercepts the
     * command message event fails the command will never be executed.
     *
     * @return An immutable list of command middlewares that should be invoked before the command.
     * @see com.avairebot.contracts.middleware.Middleware
     */
    public List<String> getMiddleware() {
        return Collections.emptyList();
    }

    /**
     * Get the command priority, if a command is used via mentioning the bot and
     * the trigger used is shared with another command, the command with the
     * highest priority will be used.
     *
     * @return The command priority.
     */
    public CommandPriority getCommandPriority() {
        return CommandPriority.NORMAL;
    }

    /**
     * Gets the category the command should belong to, if null is returned
     * the files package name will be used instead, for example:
     * <p>
     * com.avairebot.avaire.commands.utility.PingCommand, the 2nd package from the
     * right which in this case is utility, will be used as the category.
     *
     * @return Possibly null, or the command category.
     */
    public Category getCategory() {
        return null;
    }

    /**
     * Gets the group the command belongs to, command groups is used to
     * group together different commands in the help menu to make it
     * easier for users to find what they're looking for.
     *
     * @return Never-null, the command group the command should belong to.
     */
    @Nonnull
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MISCELLANEOUS);
    }

    /**
     * Determines if the command can be used in direct messages.
     *
     * @return true if the command can be run in DMs, false otherwise.
     */
    public final boolean isAllowedInDM() {
        return allowDM;
    }

    /**
     * The command executor, this method is invoked by the command handler
     * and the middleware stack when a user sends a message matching the
     * commands prefix and one of its command triggers.
     *
     * @param context The command message context generated using the
     *                JDA message event that invoked the command.
     * @param args    The arguments given to the command, if no arguments was given the array will just be empty.
     * @return true on success, false on failure.
     */
    public abstract boolean onCommand(CommandMessage context, String[] args);

    /**
     * Builds and sends the given error message to the
     * given channel for the JDA message object.
     *
     * @param context The command message context generated using the
     *                JDA message event that invoked the command.
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
     * @param context The command message context generated using the
     *                JDA message event that invoked the command.
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
     * @param context  The command message context generated using the
     *                 JDA message event that invoked the command.
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

    /**
     * Generates the command description, any middlewares assigned to the command
     * will also be dynamically generated and added to the command description
     * two lines below the actually description.
     *
     * @param context The command message context generated using the
     *                JDA message event that invoked the command.
     * @return The generated command description.
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
     * Generates the command usage instructions, if the {@link #getUsageInstructions()} is null
     * then the command trigger will just be returned instead inside of markdown code syntax,
     * if the usage instructions are not null, each item in the array will become a new line.
     *
     * @param message The JDA message object.
     * @return The usage instructions for the current command.
     */
    public final String generateUsageInstructions(Message message) {
        return formatCommandGeneratorString(message,
            getUsageInstructions() == null ? "`:command`" :
                getUsageInstructions().stream()
                    .collect(Collectors.joining("\n"))
        );
    }

    /**
     * Generates the example usage, if the {@link #generateExampleUsage(Message)} is null then the
     * command trigger will just be returned instead inside of markdown coe syntax.
     *
     * @param message The JDA message object.
     * @return The example usage for the current command.
     */
    public final String generateExampleUsage(Message message) {
        if (getExampleUsage() == null) {
            return formatCommandGeneratorString(message, "`:command`");
        }

        return formatCommandGeneratorString(message,
            getExampleUsage().isEmpty() ? "`:command`" : String.join("\n", getExampleUsage())
        );
    }

    /**
     * Generates the command triggers, if a custom category prefix have
     * been set for the current server then the correct category
     * prefix will be added to the command trigger.
     *
     * @param message The JDA message object.
     * @return The first command trigger from the command triggers method.
     */
    public final String generateCommandTrigger(Message message) {
        return generateCommandPrefix(message) + getTriggers().get(0);
    }

    /**
     * Generates the correct command prefix for the command, if no custom command
     * prefix has been set the default command prefix will be used instead.
     *
     * @param message The JDA message object.
     * @return The dynamic command prefix for the current server.
     */
    @SuppressWarnings("ConstantConditions")
    public final String generateCommandPrefix(Message message) {
        return CategoryHandler.fromCommand(this).getPrefix(message);
    }

    /**
     * Checks if the given command matches the current command by comparing the name,
     * description, usage instructions, example usage and command triggers.
     *
     * @param command The command that should be compared with the current class.
     * @return <code>True</code> if it is the same command, <code>False</code> otherwise.
     */
    public final boolean isSame(Command command) {
        return Objects.equals(command.getName(), getName())
            && Objects.equals(command.getUsageInstructions(), getUsageInstructions())
            && Objects.equals(command.getExampleUsage(), getExampleUsage())
            && Objects.equals(command.getTriggers(), getTriggers());
    }

    /**
     * Formats the command generated string by replacing any placeholder variables
     * that might exists in the given string with their actually values.
     *
     * @param message The JDA message object.
     * @param string  The string that should be formatted.
     * @return The formatted string.
     */
    private String formatCommandGeneratorString(Message message, String string) {
        return StringReplacementUtil.replaceAll(string, ":command", generateCommandTrigger(message));
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

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Command && isSame((Command) obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this);
    }
}

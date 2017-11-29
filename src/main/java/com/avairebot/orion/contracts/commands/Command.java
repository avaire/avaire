package com.avairebot.orion.contracts.commands;

import com.avairebot.orion.Orion;
import com.avairebot.orion.chat.MessageType;
import com.avairebot.orion.commands.*;
import com.avairebot.orion.contracts.reflection.Reflectionable;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.middleware.Middleware;
import com.avairebot.orion.permissions.Permissions;
import com.avairebot.orion.plugin.JavaPlugin;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public abstract class Command extends Reflectionable {


    /**
     * Determines if the command can be used in direct messages or not.
     */
    protected final boolean allowDM;

    /**
     * Create the given command instance by calling {@link #Command(Orion)} with the orion instance and allowDM set to true.
     *
     * @param plugin The plugin instance that is registering the command.
     */
    public Command(JavaPlugin plugin) {
        this(plugin.getOrion());
    }

    /**
     * Create the given command instance by calling {@link #Command(Orion, boolean)} with the orion instance.
     *
     * @param plugin  The plugin instance that is registering the command.
     * @param allowDM Determines if the command can be used in DMs.
     */
    public Command(JavaPlugin plugin, boolean allowDM) {
        this(plugin.getOrion(), allowDM);
    }

    /**
     * Creates the given command instance by calling {@link #Command(Orion, boolean)} with allowDM set to true.
     *
     * @param orion The Orion class instance.
     */
    public Command(Orion orion) {
        this(orion, true);
    }

    /**
     * Creates the given command instance with the given
     * Orion instance and the allowDM settings.
     *
     * @param orion   The Orion class instance.
     * @param allowDM Determines if the command can be used in DMs.
     */
    public Command(Orion orion, boolean allowDM) {
        super(orion);

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
     *
     * @return Never-null, the command description.
     */
    public abstract String getDescription();

    /**
     * Gets the command usage instructions for the given command, if the usage instructions
     * is set to null the {@link #generateUsageInstructions(Message)} method will just
     * return the command trigger in code syntax quotes.
     *
     * @return Possibly-null, the command usage instructions.
     */
    public abstract List<String> getUsageInstructions();

    /**
     * Get the example usage for the given command, this is used to help users with
     * using the command by example, if the example usage is set to null the
     * {@link #generateExampleUsage(Message)} method will just return the
     * command trigger in code syntax quotes.
     *
     * @return Possibly-null, an example of how to use the command.
     */
    public abstract String getExampleUsage();

    /**
     * Gets am immutable list of command triggers that can be used to invoke the current
     * command, the first index in the list will be used when the `:command` placeholder
     * is used in {@link #getDescription()} or {@link #getUsageInstructions()} methods.
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
     * @see com.avairebot.orion.middleware.Middleware
     */
    public List<String> getMiddleware() {
        return new ArrayList<>();
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
     * com.avairebot.orion.commands.utility.PingCommand, the 2nd package from the
     * right which in this case is utility, will be used as the category.
     *
     * @return Possibly null, or the command category.
     */
    public Category getCategory() {
        return null;
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
     * @param message The JDA message object from the message received event.
     * @param args    The arguments given to the command, if no arguments was given the array will just be empty.
     * @return true on success, false on failure.
     */
    public abstract boolean onCommand(Message message, String[] args);

    /**
     * Builds and sends the given error message to the
     * given channel for the JDA message object.
     *
     * @param message The JDA message object.
     * @param error   The error message that should be sent.
     * @return false since the error message should only be used on failure.
     */
    public final boolean sendErrorMessage(Message message, String error) {
        Category category = CategoryHandler.fromCommand(this);

        message.getChannel().sendMessage(MessageFactory.createEmbeddedBuilder()
            .setTitle(getName())
            .setDescription(error)
            .setColor(MessageType.ERROR.getColor())
            .addField("Usage", generateUsageInstructions(message), false)
            .addField("Example Usage", generateExampleUsage(message), false)
            .setFooter("Command category: " + category.getName(), null).build()).queue();

        return false;
    }

    /**
     * Builds and sends the given error message to the
     * given channel for the JDA message object.
     *
     * @param message The JDA message object.
     * @param error   The error message that should be sent.
     * @param args    The array of arguments that should be replace in the error string.
     * @return false since the error message should only be used on failure.
     */
    protected final boolean sendErrorMessage(Message message, String error, String... args) {
        return sendErrorMessage(message, String.format(error, args));
    }

    /**
     * Generates the command description, any middlewares assigned to the command
     * will also be dynamically generated and added to the command description
     * two lines below the actually description.
     *
     * @param message The JDA message object.
     * @return The generated command description.
     */
    public final String generateDescription(Message message) {
        if (getMiddleware().isEmpty()) {
            return getDescription().trim();
        }

        List<String> description = new ArrayList<>();
        description.add(getDescription());
        description.add("");

        MIDDLEWARE_LOOP:
        for (String middleware : getMiddleware()) {
            String[] split = middleware.split(":");
            AtomicReference<Middleware> middlewareReference = new AtomicReference<>(Middleware.fromName(split[0]));

            switch (middlewareReference.get()) {
                case IS_BOT_ADMIN:
                    description.add("**You must be a Bot Administrator to use this command!**");
                    break MIDDLEWARE_LOOP;

                case THROTTLE:
                    String[] args = split[1].split(",");
                    description.add(String.format("**This command can only be used `%s` time(s) every `%s` seconds per %s**",
                        args[1], args[2], args[0].equalsIgnoreCase("guild") ? "server" : args[0]
                    ));
                    break;

                case HAS_ROLE:
                    String[] roles = split[1].split(",");
                    if (roles.length == 1) {
                        description.add(String.format("**The `%s` role is required to use this command!**", roles[0]));
                        break;
                    }
                    description.add(String.format("**The `%s` roles is required to use this command!**",
                        String.join("`, `", roles)
                    ));
                    break;

                case REQUIRE:
                    String[] nodes = split[1].split(",");
                    nodes = Arrays.copyOfRange(nodes, 1, nodes.length);
                    if (nodes.length == 1) {
                        description.add(String.format("**The `%s` permission is required to use this command!**",
                            Permissions.fromNode(nodes[0]).getPermission().getName()
                        ));
                        break;
                    }
                    description.add(String.format("**The `%s` permissions is required to use this command!**",
                        Arrays.asList(nodes).stream()
                            .map(Permissions::fromNode)
                            .map(Permissions::getPermission)
                            .map(Permission::getName)
                            .collect(Collectors.joining("`, `"))
                    ));
                    break;
            }
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
        return formatCommandGeneratorString(message,
            getExampleUsage() == null ? "`:command`" : getExampleUsage()
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
    public final String generateCommandPrefix(Message message) {
        GuildTransformer transformer = GuildController.fetchGuild(orion, message);
        Category category = CategoryHandler.fromCommand(this);

        return transformer == null ? category.getPrefix() : transformer.getPrefixes().getOrDefault(
            category.getName().toLowerCase(),
            category.getPrefix()
        );
    }

    /**
     * Checks if the given command matches the current command by comparing the name,
     * description, usage instructions, example usage and command triggers.
     *
     * @param command The command that should be compared with the current class.
     * @return
     */
    public final boolean isSame(Command command) {
        return Objects.equals(command.getName(), getName())
            && Objects.equals(command.getDescription(), command.getDescription())
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
        CommandContainer container = CommandHandler.getCommand(this);
        String command = generateCommandPrefix(message) + container.getCommand().getTriggers().get(0);

        return string.replaceAll(":command", command);
    }
}

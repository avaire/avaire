package com.avairebot.commands.help;

import com.avairebot.AvaIre;
import com.avairebot.chat.MessageType;
import com.avairebot.commands.*;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.MessageFactory;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends Command {

    public HelpCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Help Command";
    }

    @Override
    public String getDescription() {
        return "Tells you about what commands AvaIre has, what they do, and how you can use them.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Shows a list of command categories.",
            "`:command <category>` - Shows a list of commands in the given category.",
            "`:command <command>` - Shows detailed information on how to use the given command."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("help");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return showCategories(context);
        }

        CommandContainer command = getCommand(context, args[0]);
        if (command == null) {
            return showCategoryCommands(context, CategoryHandler.fromLazyName(args[0], false), args[0]);
        }

        return showCommand(context, command, args[0]);
    }

    private boolean showCategories(CommandMessage context) {
        Category category = CategoryHandler.random(false);

        String note = String.format(":information_source: Type `:help <category>` to get a list of commands in that category.\nExample: `:help %s` or `:help %s`",
            category.getName().toLowerCase(),
            category.getName().toLowerCase().substring(0, 3)
        ).replaceAll(":help", generateCommandTrigger(context.getMessage()));

        context.makeInfo(getCategories(context.getMessage()) + note)
            .setTitle(":scroll: Command Categories")
            .queue();

        return true;
    }

    private boolean showCategoryCommands(CommandMessage context, Category category, String categoryString) {
        if (category == null) {
            context.makeError("Invalid command category given, there are no categories called `:category`")
                .set("category", categoryString)
                .queue();
            return false;
        }

        boolean isBotAdmin = avaire.getConfig().getStringList("botAccess").contains(context.getAuthor().getId());
        if (!isBotAdmin && category.getName().equalsIgnoreCase("System")) {
            context.makeError("You don't have permissions to run any of the  commands in the `System` " +
                "category, system commands can affect all the servers the bot is currently running on, and thus are " +
                "limited to bot administrators/developers.")
                .queue();
            return false;
        }

        context.getMessageChannel().sendMessage(String.format(
            ":page_with_curl: **%s** ```css\n%s```\n",
            "List of Commands",
            CommandHandler.getCommands().stream()
                .filter(commandContainer -> {
                    if (commandContainer.getPriority().equals(CommandPriority.HIDDEN)) {
                        return false;
                    }

                    if (!isBotAdmin && commandContainer.getPriority().equals(CommandPriority.SYSTEM)) {
                        return false;
                    }

                    return commandContainer.getCategory().equals(category);
                })
                .map(container -> {
                    String trigger = container.getCommand().generateCommandTrigger(context.getMessage());

                    for (int i = trigger.length(); i < 16; i++) {
                        trigger += " ";
                    }

                    List<String> triggers = container.getCommand().getTriggers();
                    if (triggers.size() == 1) {
                        return trigger + "[]";
                    }

                    String prefix = container.getCommand().generateCommandPrefix(context.getMessage());
                    String[] aliases = new String[triggers.size() - 1];
                    for (int i = 1; i < triggers.size(); i++) {
                        aliases[i - 1] = prefix + triggers.get(i);
                    }
                    return String.format("%s[%s]", trigger, String.join(", ", aliases));
                })
                .sorted()
                .collect(Collectors.joining("\n"))
        )).queue(sentMessage -> MessageFactory.makeInfo(sentMessage,
            "**Type `:help <command>` to see the help for that specified command.**\nExample: `:help :command`"
                .replaceAll(":help", generateCommandTrigger(context.getMessage()))
                .replace(":command", CommandHandler.getCommands().stream()
                    .filter(commandContainer -> commandContainer.getCategory().equals(category))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                        Collections.shuffle(collected);
                        return collected.stream();
                    }))
                    .findFirst().get().getCommand().generateCommandTrigger(context.getMessage())
                )
        ).queue());

        return true;
    }

    private boolean showCommand(CommandMessage context, CommandContainer command, String commandString) {
        if (command == null) {
            context.makeError("Invalid command given, there are no command that has the trigger `:trigger`")
                .set("trigger", commandString)
                .queue();
            return false;
        }

        final String commandPrefix = command.getCommand().generateCommandPrefix(context.getMessage());

        EmbedBuilder embed = MessageFactory.createEmbeddedBuilder()
            .setTitle(command.getCommand().getName())
            .setColor(MessageType.SUCCESS.getColor())
            .addField("Usage", command.getCommand().generateUsageInstructions(context.getMessage()), false)
            .setFooter("Command category: " + command.getCategory().getName(), null);

        if (command.getCommand().getTriggers().size() > 1) {
            embed.addField("Aliases", command.getCommand().getTriggers().stream()
                .skip(1)
                .map(trigger -> commandPrefix + trigger)
                .collect(Collectors.joining("`, `", "`", "`")), false);
        }

        context.getMessageChannel().sendMessage(embed.setDescription(
            command.getCommand().generateDescription(context.getMessage())
        ).build()).queue();
        return true;
    }

    private CommandContainer getCommand(CommandMessage context, String commandString) {
        CommandContainer command = CommandHandler.getCommand(context.getMessage(), commandString);
        if (command != null) {
            return command;
        }
        return CommandHandler.getLazyCommand(commandString);
    }

    private String getCategories(Message message) {
        boolean isBotAdmin = avaire.getConfig().getStringList("botAccess").contains(message.getAuthor().getId());

        return CategoryHandler.getValues().stream()
            .filter(category -> !category.isGlobal())
            .map(Category::getName)
            .sorted()
            .filter(category -> isBotAdmin || !category.equalsIgnoreCase("System"))
            .collect(Collectors.joining("\n• ", "• ", "\n\n"));
    }
}

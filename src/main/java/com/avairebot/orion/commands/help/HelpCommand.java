package com.avairebot.orion.commands.help;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.Category;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends Command {

    private final String categories;

    public HelpCommand(Orion orion) {
        super(orion);

        categories = Arrays.stream(Category.values())
                .map(Category::getName)
                .sorted()
                .collect(Collectors.joining("\n• ", "• ", "\n\n"));
    }

    @Override
    public String getName() {
        return "Help Command";
    }

    @Override
    public String getDescription() {
        return "Tells you about what commands Orion has, what they do, and how you can use them.";
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
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("help");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return showCategories(message);
        }

        if (!isCommand(message, args[0])) {
            return showCategoryCommands(message, Category.fromLazyName(args[0]), args[0]);
        }

        return showCommand(message, CommandHandler.getCommand(message, args[0]), args[0]);
    }

    private boolean showCategories(Message message) {
        Category category = Category.random();

        String note = String.format(":information_source: Type `:help <category>` to get a list of commands in that category.\nExample: `:help %s` or `:help %s`",
                category.getName().toLowerCase(),
                category.getName().toLowerCase().substring(0, 3)
        ).replaceAll(":help", generateCommandTrigger(message));

        MessageEmbed embed = MessageFactory.createEmbeddedBuilder()
                .setColor(MessageFactory.MessageType.INFO.getColor())
                .setTitle(":scroll: Command Categories")
                .setDescription(categories + note)
                .build();

        message.getChannel().sendMessage(embed).queue();
        return true;
    }

    private boolean showCategoryCommands(Message message, Category category, String categoryString) {
        if (category == null) {
            MessageFactory.makeError(message, "Invalid command category given, there are no categories called `%s`", categoryString).queue();
            return false;
        }

        message.getChannel().sendMessage(String.format(
                ":page_with_curl: **%s** ```css\n%s```\n",
                "List of Commands",
                CommandHandler.getCommands().stream()
                        .filter(commandContainer -> commandContainer.getCategory().equals(category))
                        .map(container -> {
                            String trigger = container.getCommand().generateCommandTrigger(message);

                            for (int i = trigger.length(); i < 15; i++) {
                                trigger += " ";
                            }

                            List<String> triggers = container.getCommand().getTriggers();
                            if (triggers.size() == 1) {
                                return trigger + "[]";
                            }

                            String prefix = container.getCommand().generateCommandPrefix(message);
                            String[] aliases = new String[triggers.size() - 1];
                            for (int i = 1; i < triggers.size(); i++) {
                                aliases[i - 1] = prefix + triggers.get(i);
                            }
                            return String.format("%s[%s]", trigger, String.join(", ", aliases));
                        })
                        .collect(Collectors.joining("\n"))
        )).queue(sentMessage -> MessageFactory.makeInfo(sentMessage,
                "**Type `:help <command>` to see the help for that specified command.**\nExample: `:help :command`"
                        .replaceAll(":help", generateCommandTrigger(message))
                        .replace(":command", CommandHandler.getCommands().stream()
                                .filter(commandContainer -> commandContainer.getCategory().equals(category))
                                .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                                    Collections.shuffle(collected);
                                    return collected.stream();
                                }))
                                .findFirst().get().getCommand().generateCommandTrigger(message)
                        )
        ).queue());

        return true;
    }

    private boolean showCommand(Message message, CommandContainer command, String commandString) {
        if (command == null) {
            MessageFactory.makeError(message, "Invalid command given, there are no command that has the trigger `%s`", commandString).queue();
            return false;
        }

        final String commandPrefix = command.getCommand().generateCommandPrefix(message);

        EmbedBuilder embed = MessageFactory.createEmbeddedBuilder()
                .setTitle(command.getCommand().getName())
                .setColor(MessageFactory.MessageType.SUCCESS.getColor())
                .addField("Usage", command.getCommand().generateUsageInstructions(message), false)
                .setFooter("Command category: " + command.getCategory().getName(), null);

        StringBuilder description = embed.getDescriptionBuilder()
                .append(command.getCommand().generateDescription(message));

        if (command.getCommand().getTriggers().size() > 1) {
            embed.addField("Aliases", command.getCommand().getTriggers().stream()
                    .skip(1)
                    .map(trigger -> commandPrefix + trigger)
                    .collect(Collectors.joining("`, `", "`", "`")), false);
        }


        message.getChannel().sendMessage(embed.setDescription(description).build()).queue();
        return true;
    }

    private boolean isCommand(Message message, String commandString) {
        CommandContainer command = CommandHandler.getCommand(message, commandString);
        return command != null && commandString.startsWith(command.getCommand().generateCommandPrefix(message));
    }
}

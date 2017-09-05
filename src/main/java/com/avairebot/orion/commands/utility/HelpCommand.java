package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.Category;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends AbstractCommand {

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
                "`!help` - Shows a list of command categories.",
                "`!help <category>` - Shows a list of commands in the given category.",
                "`!help <command>` - Shows detailed information on how to use the given command."

        );
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

        if (!isCommand(args[0])) {
            return showCategoryCommands(message, Category.fromLazyName(args[0]), args[0]);
        }

        return showCommand(message, CommandHandler.getCommand(args[0]), args[0]);
    }

    private boolean showCategories(Message message) {
        Category category = Category.random();

        String note = String.format(
                ":information_source: Type `!help <category>` to get a list of commands in that category.\nExample: `!help %s` or `!help %s`",
                category.getName().toLowerCase(),
                category.getName().toLowerCase().substring(0, 3)
        );

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
                        .map(container -> container.getDefaultPrefix() + container.getCommand().getTriggers().get(0))
                        .collect(Collectors.joining("\n"))
        )).queue(sentMessage -> MessageFactory.makeInfo(sentMessage,
                "**Type `:help <command>` to see the help for that specified command.**\\nExample: `:help !fillerCommand`"
        ).queue());

        return true;
    }

    private boolean showCommand(Message message, CommandContainer command, String commandString) {
        if (command == null) {
            MessageFactory.makeError(message, "Invalid command given, there are no command that has the trigger `%s`", commandString).queue();
            return false;
        }

        EmbedBuilder embed = MessageFactory.createEmbeddedBuilder()
                .setTitle(command.getCommand().getName())
                .setColor(MessageFactory.MessageType.SUCCESS.getColor())
                .addField("Usage", command.getCommand().getUsageInstructions().stream().collect(Collectors.joining("\n")), false)
                .setFooter("Command category: " + command.getCategory().getName(), null);

        StringBuilder description = embed.getDescriptionBuilder()
                .append(command.getCommand().getDescription());

        if (command.getCommand().getTriggers().size() > 1) {
            embed.addField("Aliases", command.getCommand().getTriggers().stream()
                    .skip(1)
                    .map(trigger -> command.getDefaultPrefix() + trigger)
                    .collect(Collectors.joining("`, `", "`", "`")), false);
        }


        message.getChannel().sendMessage(embed.setDescription(description).build()).queue();
        return true;
    }

    private boolean isCommand(String command) {
        for (Category category : Category.values()) {
            if (command.startsWith(category.getPrefix()) && CommandHandler.getCommand(command) != null) {
                return true;
            }
        }
        return false;
    }
}

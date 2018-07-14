package com.avairebot.commands.help;

import com.avairebot.AvaIre;
import com.avairebot.chat.MessageType;
import com.avairebot.commands.*;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.language.I18n;
import com.avairebot.utilities.StringReplacementUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        String note = StringReplacementUtil.replaceAll(
            context.i18n("categoriesNote",
                category.getName().toLowerCase(),
                category.getName().toLowerCase().substring(0, 3)
            ), ":help", generateCommandTrigger(context.getMessage())
        );

        context.makeInfo(getCategories(context) + note)
            .setTitle(context.i18n("categoriesTitle"))
            .queue();

        return true;
    }

    private boolean showCategoryCommands(CommandMessage context, Category category, String categoryString) {
        if (category == null) {
            context.makeError(context.i18n("invalidCategory"))
                .set("category", categoryString)
                .queue();
            return false;
        }

        boolean isBotAdmin = avaire.getBotAdmins().contains(context.getAuthor().getId());
        if (!isBotAdmin && category.getName().equalsIgnoreCase("System")) {
            context.makeError(context.i18n("tryingToViewSystemCommands"))
                .queue();
            return false;
        }

        Optional<CommandContainer> randomCommandFromCategory = CommandHandler.getCommands().stream()
            .filter(commandContainer -> commandContainer.getCategory().equals(category))
            .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                Collections.shuffle(collected);
                return collected.stream();
            })).findFirst();

        //noinspection ConstantConditions
        context.getMessageChannel().sendMessage(
            new MessageBuilder()
                // Builds and sets the content of the message, this is all the
                // commands for the given category the command was used for.
                .setContent(context.i18n("listOfCommands",
                    CommandHandler.getCommands().stream()
                        .filter(container -> filterCommandContainer(container, category, isBotAdmin))
                        .map(container -> mapCommandContainer(context, container))
                        .sorted()
                        .collect(Collectors.joining("\n"))
                ))
                // Builds and sets the embedded tip/note, giving people an example
                // of how get information for the specific command.
                .setEmbed(MessageFactory.createEmbeddedBuilder()
                    .setColor(MessageType.INFO.getColor())
                    .setDescription(StringReplacementUtil.replaceAll(
                        StringReplacementUtil.replaceAll(
                            context.i18n("commandNote"),
                            ":help", generateCommandTrigger(context.getMessage())
                        ), ":command", randomCommandFromCategory.isPresent() ?
                            randomCommandFromCategory.get().getCommand().getTriggers().get(0) : "Unknown")
                    )
                    .build()
                ).build()
        ).queue();

        return true;
    }

    private boolean showCommand(CommandMessage context, CommandContainer command, String commandString) {
        if (command == null) {
            context.makeError(context.i18n("invalidCommand"))
                .set("trigger", commandString)
                .queue();
            return false;
        }

        final String commandPrefix = command.getCommand().generateCommandPrefix(context.getMessage());

        EmbedBuilder embed = MessageFactory.createEmbeddedBuilder()
            .setTitle(command.getCommand().getName())
            .setColor(MessageType.SUCCESS.getColor())
            .addField(context.i18n("fields.usage"), command.getCommand().generateUsageInstructions(context.getMessage()), false)
            .addField(context.i18n("fields.example"), command.getCommand().generateExampleUsage(context.getMessage()), false)
            .setFooter(context.i18n("fields.footer") + command.getCategory().getName(), null);

        if (command.getCommand().getTriggers().size() > 1) {
            embed.addField(
                context.i18n("fields.aliases"),
                command.getCommand().getTriggers().stream()
                    .skip(1)
                    .map(trigger -> commandPrefix + trigger)
                    .collect(Collectors.joining("`, `", "`", "`")),
                false
            );
        }

        if (command.getCommand().getRelations() != null && !command.getCommand().getRelations().isEmpty()) {
            embed.addField(
                context.i18n("fields.seeAlso"),
                command.getCommand().getRelations().stream().map(relation -> {
                    CommandContainer container = CommandHandler.getCommand(relation);
                    if (container == null) {
                        return null;
                    }
                    return container.getCommand().generateCommandTrigger(context.getMessage());
                }).filter(Objects::nonNull).collect(
                    Collectors.joining("`, `", "`", "`")
                ),
                false
            );
        }

        context.getMessageChannel().sendMessage(embed.setDescription(
            command.getCommand().generateDescription(
                new CommandMessage(command, context.getDatabaseEventHolder(), context.getMessage())
                    .setI18n(context.getI18n())
            )
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

    private String getCategories(CommandMessage context) {
        boolean isBotAdmin = avaire.getBotAdmins().contains(context.getAuthor().getId());

        List<Category> categories = CategoryHandler.getValues().stream()
            .filter(category -> !category.isGlobal())
            .collect(Collectors.toList());

        if (context.getGuild() == null) {
            return formatCategoriesStream(categories.stream(), isBotAdmin);
        }

        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return formatCategoriesStream(categories.stream(), isBotAdmin);
        }

        ChannelTransformer channel = transformer.getChannel(context.getChannel().getId());
        if (channel == null) {
            return formatCategoriesStream(categories.stream(), isBotAdmin);
        }

        long before = categories.size();
        List<Category> filteredCategories = categories.stream()
            .filter(channel::isCategoryEnabled)
            .collect(Collectors.toList());

        long disabled = before - filteredCategories.size();

        return formatCategoriesStream(
            filteredCategories.stream().filter(channel::isCategoryEnabled),
            isBotAdmin,
            disabled != 0 ? I18n.format(
                "\n\n" + (disabled == 1 ?
                    context.i18n("singularHiddenCategories") :
                    context.i18n("multipleHiddenCategories")
                ) + "\n",
                disabled
            ) : "\n\n");
    }

    private String formatCategoriesStream(Stream<Category> stream, boolean isBotAdmin) {
        return formatCategoriesStream(stream, isBotAdmin, "\n\n");
    }

    private String formatCategoriesStream(Stream<Category> stream, boolean isBotAdmin, String suffix) {
        return stream
            .map(Category::getName)
            .sorted()
            .filter(category -> isBotAdmin || !category.equalsIgnoreCase("System"))
            .collect(Collectors.joining("\n• ", "• ", suffix));
    }

    private boolean filterCommandContainer(CommandContainer container, Category category, boolean isBotAdmin) {
        if (container.getPriority().equals(CommandPriority.HIDDEN)) {
            return false;
        }

        if (!isBotAdmin && container.getPriority().equals(CommandPriority.SYSTEM)) {
            return false;
        }

        return container.getCategory().equals(category);
    }

    private String mapCommandContainer(CommandMessage context, CommandContainer container) {
        StringBuilder trigger = new StringBuilder(container.getCommand().generateCommandTrigger(context.getMessage()));

        for (int i = trigger.length(); i < 16; i++) {
            trigger.append(" ");
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
        return String.format("%s[%s]", trigger.toString(), String.join(", ", aliases));
    }
}

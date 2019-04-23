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

package com.avairebot.commands.help;

import com.avairebot.AvaIre;
import com.avairebot.admin.AdminUser;
import com.avairebot.chat.MessageType;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.commands.*;
import com.avairebot.commands.utility.SourceCommand;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.language.I18n;
import com.avairebot.utilities.StringReplacementUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

import javax.annotation.Nonnull;
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
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(SourceCommand.class);
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command play`,",
            "`:command help`",
            "`:command`"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("help");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            if (!avaire.getSettings().isMusicOnlyMode()) {
                return showCategories(context);
            }
            return showCategoryCommands(context, CategoryHandler.fromLazyName("music"), "music");
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
        if (category == null || !category.hasCommands()) {
            context.makeError(context.i18n("invalidCategory"))
                .set("category", categoryString)
                .queue();
            return false;
        }

        AdminUser adminUser = avaire.getBotAdmins().getUserById(context.getAuthor().getIdLong());
        if (isSystemCategory(category.getName()) && !adminUser.isAdmin()) {
            context.makeError(context.i18n("tryingToViewSystemCommands"))
                .queue();
            return false;
        }

        // Gets a random command from the command category, this is used
        // in the command note to show a random command as the
        // example for how to use the command.
        Optional<CommandContainer> randomCommandFromCategory = CommandHandler.getCommands().stream()
            .filter(commandContainer -> commandContainer.getCategory().equals(category))
            .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                Collections.shuffle(collected);
                return collected.stream();
            })).findFirst();

        // Creates the embedded message object with a blue colour.
        PlaceholderMessage message = context.makeEmbeddedMessage(MessageType.INFO)
            .setTitle(context.i18n("listOfCommands"));

        // Filters down the commands to just the ones belonging to the command category,
        // and adding them to the commands map by their command group.
        Map<CommandGroup, List<CommandContainer>> commands = new HashMap<>();
        CommandHandler.getCommands().stream()
            .filter(container -> filterCommandContainer(container, category, adminUser))
            .forEach(container -> {
                for (CommandGroup group : container.getCommand().getGroups()) {
                    if (!commands.containsKey(group)) {
                        commands.put(group, new ArrayList<>());
                    }
                    commands.get(group).add(container);
                }
            });

        // Creates the message embedded fields with their title,
        // value, and dynamically set inline value.
        List<MessageEmbed.Field> fields = new ArrayList<>();
        commands.forEach((key, value) -> {
            String stringifiedCommands = mapCommandContainers(context, value);
            if (stringifiedCommands.endsWith("-")) {
                fields.add(new MessageEmbed.Field(
                    key.getName(), stringifiedCommands.substring(0, stringifiedCommands.length() - 1), false)
                );
            } else {
                fields.add(new MessageEmbed.Field(
                    key.getName(), stringifiedCommands, true
                ));
            }
        });

        // Sorts the fields by line-break length and adding them to the message.
        fields.stream().sorted((l1, l2) -> {
            if (l1.getValue().split("\n").length == l2.getValue().split("\n").length) {
                return 0;
            }
            return l1.getValue().split("\n").length > l2.getValue().split("\n").length ? -1 : 1;
        }).forEach(message::addField);

        // Builds the note, adds it as a field to the bottom
        // of the command, and then sends it off to Discord.
        message.addField("", context.makeEmbeddedMessage()
            .setDescription(context.i18n("commandNote"))
            .set("help", generateCommandTrigger(context.getMessage()))
            .set("command", randomCommandFromCategory.isPresent() ?
                randomCommandFromCategory.get().getCommand().getTriggers().get(0) : "Unknown"
            ).toString(), false)
            .queue();

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
        AdminUser adminUser = avaire.getBotAdmins().getUserById(context.getAuthor().getIdLong());

        List<Category> categories = CategoryHandler.getValues().stream()
            .filter(category -> !category.isGlobal())
            .filter(Category::hasCommands)
            .collect(Collectors.toList());

        if (context.getGuild() == null) {
            return formatCategoriesStream(categories.stream(), adminUser);
        }

        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return formatCategoriesStream(categories.stream(), adminUser);
        }

        ChannelTransformer channel = transformer.getChannel(context.getChannel().getId());
        if (channel == null) {
            return formatCategoriesStream(categories.stream(), adminUser);
        }

        long before = categories.size();
        List<Category> filteredCategories = categories.stream()
            .filter(channel::isCategoryEnabled)
            .collect(Collectors.toList());

        long disabled = before - filteredCategories.size();

        return formatCategoriesStream(
            filteredCategories.stream().filter(channel::isCategoryEnabled),
            adminUser,
            disabled != 0 ? I18n.format(
                "\n\n" + (disabled == 1 ?
                    context.i18n("singularHiddenCategories") :
                    context.i18n("multipleHiddenCategories")
                ) + "\n",
                disabled
            ) : "\n\n");
    }

    private String formatCategoriesStream(Stream<Category> stream, AdminUser adminUser) {
        return formatCategoriesStream(stream, adminUser, "\n\n");
    }

    private String formatCategoriesStream(Stream<Category> stream, AdminUser adminUser, String suffix) {
        return stream
            .map(Category::getName)
            .sorted()
            .filter(category -> adminUser.isAdmin() || !isSystemCategory(category))
            .collect(Collectors.joining("\n• ", "• ", suffix));
    }

    private boolean filterCommandContainer(CommandContainer container, Category category, AdminUser adminUser) {
        if (container.getPriority().equals(CommandPriority.HIDDEN)) {
            return false;
        }

        if (!adminUser.isAdmin() && container.getPriority().isSystem()) {
            return false;
        }

        return (!adminUser.isAdmin()
            || !Objects.equals(adminUser.getCommandScope(), CommandPriority.SYSTEM_ROLE)
            || !container.getPriority().equals(CommandPriority.SYSTEM)
        ) && container.getCategory().equals(category);
    }

    @Nonnull
    private String mapCommandContainers(CommandMessage context, List<CommandContainer> containers) {
        boolean canBreak = true;
        List<String> lines = new ArrayList<>();
        StringBuilder message = new StringBuilder("```css\n");

        for (CommandContainer container : containers) {
            String trigger = container.getCommand().generateCommandTrigger(context.getMessage());
            if (trigger.length() > 16) {
                canBreak = false;
            }
            lines.add(trigger);
        }

        Collections.sort(lines);

        return message
            .append(String.join("\n", lines))
            .append("```")
            .append(canBreak ? ' ' : '-')
            .toString();
    }

    private boolean isSystemCategory(String name) {
        return name.equalsIgnoreCase("System");
    }
}

/*
 * Copyright (c) 2019.
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

package com.avairebot.commands;

import com.avairebot.contracts.commands.Command;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class CommandDataContext {

    /**
     * The name of the command.
     */
    private final String name;

    /**
     * The command description.
     */
    private final String description;

    /**
     * The usage examples for the command, telling people how to use the command.
     */
    private final List<String> usage;

    /**
     * The command examples, showing some examples for how to use the command.
     */
    private final List<String> example;

    /**
     * The command triggers which can be used to invoke the command.
     */
    private final List<String> triggers;

    /**
     * The middlewares used by the command.
     */
    private final List<String> middlewares;

    /**
     * The command relationships with other commands.
     */
    private final List<String> relationships;

    /**
     * The command priority.
     */
    private final CommandPriority priority;

    CommandDataContext(CommandContainer container) {
        this.name = container.getCommand().getName();
        this.description = container.getCommand().getDescription(null);
        this.usage = container.getCommand().getUsageInstructions(null);
        this.example = container.getCommand().getExampleUsage(null);
        this.triggers = container.getCommand().getTriggers();
        this.middlewares = container.getCommand().getMiddleware();
        this.priority = container.getCommand().getCommandPriority();

        if (container.getCommand().getRelations() != null) {
            this.relationships = container.getCommand().getRelations().stream()
                .map(clazz -> {
                    CommandContainer relatedCommand = CommandHandler.getCommand(clazz);
                    if (relatedCommand == null) {
                        return "Unknown::" + clazz.getSimpleName();
                    }
                    return relatedCommand.getCategory().getName() + "::" + clazz.getSimpleName();
                })
                .collect(Collectors.toList());
        } else {
            this.relationships = null;
        }
    }

    /**
     * Gets the name of the command, this is the same name that is displayed
     * to users when they use the !help command for a command.
     *
     * @return The name of the command.
     */
    public String getName() {
        return name;
    }

    /**
     * The description of the command, this is the same description that is
     * displayed to users when they use the !help command for a command.
     *
     * @return The command description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the command usage examples for the command, the examples are used to tell
     * the user about the arguments and options that are available with the command.
     *
     * @return The command usage examples.
     */
    public List<String> getUsage() {
        return usage;
    }

    /**
     * Gets the command examples, the examples are used to display actual uses of the
     * command to the user to give them an idea of how to use the command correctly.
     *
     * @return The command examples.
     */
    public List<String> getExample() {
        return example;
    }

    /**
     * Gets the command triggers, these command triggers can be used to invoke the command,
     * the command triggers does not contain the prefix used, only the trigger name.
     *
     * @return The command triggers.
     */
    public List<String> getTriggers() {
        return triggers;
    }

    /**
     * Gets the command middlewares, the middlewares defined for the command will run
     * both before and after the command has finished running, only the stringified
     * version of the middlewares is listed.
     * <p>
     * See: {@link Command#getMiddleware() command getMiddleware() method}.
     *
     * @return The command middlewares.
     */
    public List<String> getMiddlewares() {
        return middlewares;
    }

    /**
     * Gets the command relationships, the relationships are full package path
     * to commands that the command has some sort of relationship with.
     *
     * @return The command relationships.
     */
    public List<String> getRelationships() {
        return relationships;
    }

    /**
     * Gets the command priority, the higher the command priority is, the more
     * important it is, this priority is used to determine which command
     * should be invoked if two commands share a command trigger.
     *
     * @return The command priority.
     */
    public CommandPriority getPriority() {
        return priority;
    }
}

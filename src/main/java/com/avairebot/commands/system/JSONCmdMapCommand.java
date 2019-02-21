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

package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.commands.*;
import com.avairebot.contracts.commands.SystemCommand;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class JSONCmdMapCommand extends SystemCommand {

    public JSONCmdMapCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "JSON Command Map";
    }

    @Override
    public String getDescription() {
        return "Creates a JSON map containing detailed information about each command and stores it in a `commandMap.json` file.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Generates the command map and stores it in a file.");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("jsoncmdmap");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Category category : CategoryHandler.getValues()) {
            Map<String, Object> categoryMap = new LinkedHashMap<>();
            Map<String, JSONCommand> categoryCommands = new LinkedHashMap<>();

            for (CommandContainer container : CommandHandler.getCommands().stream()
                .filter(container -> container.getCategory().equals(category))
                .sorted(Comparator.comparing(container -> container.getCommand().getClass().getSimpleName()))
                .collect(Collectors.toList())) {

                context.setI18nCommandPrefix(container);

                JSONCommand command = new JSONCommand();

                command.name = container.getCommand().getName();
                command.description = container.getCommand().getDescription(null);
                command.usage = container.getCommand().getUsageInstructions();
                command.example = container.getCommand().getExampleUsage();
                command.triggers = container.getCommand().getTriggers();
                command.middlewares = container.getCommand().getMiddleware();
                command.priority = container.getCommand().getCommandPriority();

                if (container.getCommand().getRelations() != null) {
                    command.relationships = container.getCommand().getRelations().stream()
                        .map(clazz -> {
                            CommandContainer relatedCommand = CommandHandler.getCommand(clazz);
                            if (relatedCommand == null) {
                                return "Unknown::" + clazz.getSimpleName();
                            }
                            return relatedCommand.getCategory().getName() + "::" + clazz.getSimpleName();
                        })
                        .collect(Collectors.toList());
                }

                categoryCommands.put(container.getCommand().getClass().getSimpleName(), command);
            }

            if (!categoryCommands.isEmpty()) {
                categoryMap.put("prefix", category.getPrefix());
                categoryMap.put("commands", categoryCommands);

                map.put(category.getName(), categoryMap);
            }
        }

        try (FileWriter file = new FileWriter("commandMap.json")) {
            file.write(AvaIre.gson.toJson(map));

            context.makeSuccess("The `commandMap.json` file has been updated with the current command information.").queue();
        } catch (IOException e) {
            AvaIre.getLogger().error("Something went wrong while trying to save the command map: {}", e.getMessage(), e);
            context.makeError("Failed to store the command map data, error: " + e.getMessage()).queue();
            return false;
        }

        return true;
    }

    private class JSONCommand {
        String name;
        String description;
        List<String> usage;
        List<String> example;
        List<String> triggers;
        List<String> middlewares;
        List<String> relationships;
        CommandPriority priority;
    }
}

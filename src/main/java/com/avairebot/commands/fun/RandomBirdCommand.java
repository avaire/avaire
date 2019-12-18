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

package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;
import com.avairebot.requests.service.RandomBirdService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class RandomBirdCommand extends Command {

    public RandomBirdCommand(AvaIre avaire) {
        super(avaire);
    }

    /**
     * Gets the command name, this is used in help and error
     * messages as the title as well as log messages.
     *
     * @return Never-null, the command name.
     */
    @Override
    public String getName() {
        return "Random Bird Command";
    }

    @Override
    public String getDescription() {
        return "I will scour the internet to find a random bird picture for you.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Gets a random picture of a bird and sends it in the channel.");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(RandomDogCommand.class, RandomCatCommand.class);
    }

    /**
     * Gets am immutable list of command triggers that can be used to invoke the current
     * command, the first index in the list will be used when the `:command` placeholder
     * is used in {@link #getDescription(CommandContext)} or {@link #getUsageInstructions()} methods.
     *
     * @return An immutable list of command triggers that should invoked the command.
     */
    @Override
    public List<String> getTriggers() {
        return Arrays.asList("randombird", "bird", "birb", "randombirb");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,2,5");
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
    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        RequestFactory.makeGET("http://random.birb.pw/tweet.json/")
            .send((Consumer<Response>) response -> {
                RandomBirdService service = (RandomBirdService) response.toService(RandomBirdService.class);

                context.makeEmbeddedMessage().setImage("http://random.birb.pw/img/" + service.getFile()).queue();

            });
        return true;
    }


}

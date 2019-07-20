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

package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;
import com.avairebot.requests.service.RandomDogService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class RandomDogCommand extends Command {

    public RandomDogCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Random Dog Command";
    }

    @Override
    public String getDescription() {
        return "I will scour the internet to find a random dog picture for you.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Gets a random picture of a dog and sends it in the channel.");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(RandomCatCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("randomdog", "dog");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,2,5");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        RequestFactory.makeGET("https://dog.ceo/api/breeds/image/random")
            .send((Consumer<Response>) response -> {
                RandomDogService service = (RandomDogService) response.toService(RandomDogService.class);

                context.makeEmbeddedMessage().setImage(service.getMessage()).queue();
            });
        return true;
    }
}

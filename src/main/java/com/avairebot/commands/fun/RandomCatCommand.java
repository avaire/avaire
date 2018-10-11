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
import com.avairebot.requests.Request;
import com.avairebot.requests.Response;
import com.avairebot.requests.service.RandomCatService;
import com.avairebot.utilities.RestActionUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RandomCatCommand extends Command {

    public RandomCatCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Random Cat Command";
    }

    @Override
    public String getDescription() {
        return "I will scour the internet to find a random cat picture for you.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Gets a random picture of a cat and sends it in the channel.");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(RandomDogCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("randomcat", "cat");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,2,5");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        Request request = RequestFactory.makeGET("https://meow.senither.com/v1/random");

        String token = avaire.getConfig().getString("apiKeys.meowApi", null);
        if (token != null && token.length() > 0) {
            request.addParameter("token", token);
        }

        request.send((Consumer<Response>) response -> {
            int statusCode = response.getResponse().code();

            if (statusCode == 429) {
                context.makeWarning(context.i18n("tooManyAttempts"))
                    .queue(message -> message.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore));

                return;
            }

            if (statusCode == 404) {
                context.makeWarning(context.i18n("notFound"))
                    .queue(message -> message.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore));

                return;
            }

            if (statusCode == 200) {
                RandomCatService service = (RandomCatService) response.toService(RandomCatService.class);
                context.makeEmbeddedMessage().setImage(service.getData().getUrl()).queue();

                return;
            }

            context.makeError(context.i18n("somethingWentWrong")).queue();
        });
        return true;
    }
}

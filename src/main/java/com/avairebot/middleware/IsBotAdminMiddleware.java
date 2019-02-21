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

package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class IsBotAdminMiddleware extends Middleware {

    public IsBotAdminMiddleware(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String buildHelpDescription(@Nonnull String[] arguments) {
        return "**You must be a Bot Administrator to use this command!**";
    }

    @Override
    public boolean handle(@Nonnull Message message, @Nonnull MiddlewareStack stack, String... args) {
        if (avaire.getBotAdmins().getUserById(message.getAuthor().getIdLong(), true).isAdmin()) {
            return stack.next();
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("use-role")) {
            return sendMustBeBotAdminMessage(message);
        }

        if (!avaire.getBotAdmins().getUserById(message.getAuthor().getIdLong()).isAdmin()) {
            return sendMustBeBotAdminMessage(message);
        }

        return stack.next();
    }

    private boolean sendMustBeBotAdminMessage(@Nonnull Message message) {
        return runMessageCheck(message, () -> {
            MessageFactory.makeError(message, ":warning: You must be a bot administrator to use this command!")
                .queue(newMessage -> newMessage.delete().queueAfter(45, TimeUnit.SECONDS), RestActionUtil.ignore);

            return false;
        });
    }
}

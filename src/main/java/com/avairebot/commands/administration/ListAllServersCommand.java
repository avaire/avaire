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

package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.controllers.ReactionController;
import com.avairebot.language.I18n;
import com.avairebot.utilities.NumberUtil;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.core.entities.Emote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import JDA.*;

public class ListAllServersCommand extends Command {

    public ListAllServersCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "List All Servers Command";
    }

    @Override
    public String getDescription() {
        return "List all the Servers the bot is on.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command [page]` - Lists all servers."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            AddReactionRoleCommand.class,
            RemoveReactionRoleCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("lrr");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.administrator",
            "throttle:guild,1,5"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(
            CommandGroups.ROLE_ASSIGNMENTS
        );
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean onCommand(CommandMessage context, String[] args) {
        Collection collection = avaire.GetGuilds();
		for (Guild guild : collection) {
			Integer members = guild.getMembers();
			if (members < 4) {
             sendErrorMessage(guild.getName(),"test");
			 //log.info("Name: {0} and ID:", guildgetName());
			}
         }
        return false;
    }
}

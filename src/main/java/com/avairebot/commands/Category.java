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

package com.avairebot.commands;

import com.avairebot.AvaIre;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.CacheUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class Category {

    public static final Cache<Object, Object> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(2500, TimeUnit.MILLISECONDS)
        .build();

    private final AvaIre avaire;
    private final String name;
    private final String prefix;

    private boolean isGlobal = false;

    public Category(AvaIre avaire, String name, String prefix) {
        this.avaire = avaire;
        this.name = name;
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getPrefix(@Nonnull Message message) {
        if (isGlobal) {
            return getPrefix();
        }

        if (message.getGuild() == null) {
            return getPrefix();
        }

        return (String) CacheUtil.getUncheckedUnwrapped(cache, asKey(message), () -> {
            GuildTransformer transformer = GuildController.fetchGuild(avaire, message);

            return transformer == null ? getPrefix() : transformer.getPrefixes().getOrDefault(
                getName().toLowerCase(), getPrefix()
            );
        });
    }

    public boolean hasCommands() {
        return CommandHandler.getCommands().stream().
            filter(container -> container.getCategory().equals(this))
            .count() > 0;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    Category setGlobal(boolean value) {
        isGlobal = value;
        return this;
    }

    public boolean isGlobalOrSystem() {
        return isGlobal || name.equalsIgnoreCase("system");
    }

    private String asKey(Message message) {
        return message.getGuild().getId() + ":" + name;
    }
}

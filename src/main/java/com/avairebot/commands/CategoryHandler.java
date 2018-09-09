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
import com.avairebot.contracts.commands.Command;
import com.avairebot.shared.DiscordConstants;
import com.avairebot.utilities.RandomUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CategoryHandler {

    private static final List<Category> VALUES = new ArrayList<>();

    static {
        VALUES.add(new Category(
            null, "all", DiscordConstants.DEFAULT_COMMAND_PREFIX
        ).setGlobal(true));
    }

    public static boolean addCategory(@Nonnull AvaIre avaire, @Nonnull String name, @Nonnull String defaultPrefix) {
        for (Category category : VALUES) {
            if (category.getName().equalsIgnoreCase(name)) {
                return false;
            }
        }

        VALUES.add(new Category(avaire, name, defaultPrefix));
        return true;
    }

    public static Category fromLazyName(@Nonnull String name) {
        return fromLazyName(name, false);
    }

    public static Category fromLazyName(@Nonnull String name, boolean includeGlobals) {
        name = name.toLowerCase();

        for (Category category : getValues()) {
            if (!includeGlobals && category.isGlobal()) {
                continue;
            }

            if (category.getName().toLowerCase().startsWith(name)) {
                return category;
            }
        }
        return null;
    }

    public static Category fromCommand(@Nonnull Command command) {
        Category category = command.getCategory();
        if (category != null) {
            return category;
        }

        String[] path = command.getClass().getName().split("\\.");
        String commandPackage = path[path.length - 2];

        for (Category cat : getValues()) {
            if (cat.getName().equalsIgnoreCase(commandPackage)) {
                return cat;
            }
        }
        return null;
    }

    public static Category random() {
        return VALUES.get(RandomUtil.getInteger(VALUES.size()));
    }

    public static Category random(boolean includeGlobals) {
        return VALUES.stream()
            .filter(category -> !category.isGlobal())
            .findAny().orElseGet(CategoryHandler::random);
    }

    public static List<Category> getValues() {
        return VALUES;
    }
}

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

package com.avairebot.commands;

import com.avairebot.AvaIre;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;

public class Category {

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

        GuildTransformer transformer = GuildController.fetchGuild(avaire, message);

        return transformer == null ? getPrefix() : transformer.getPrefixes().getOrDefault(
            getName().toLowerCase(), getPrefix()
        );
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
}

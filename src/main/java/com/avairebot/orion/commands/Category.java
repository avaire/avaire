package com.avairebot.orion.commands;

import com.avairebot.orion.Orion;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import net.dv8tion.jda.core.entities.Message;

public class Category {

    private final Orion orion;
    private final String name;
    private final String prefix;

    public Category(Orion orion, String name, String prefix) {
        this.orion = orion;
        this.name = name;
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getPrefix(Message message) {
        GuildTransformer transformer = GuildController.fetchGuild(orion, message);

        return transformer == null ? getPrefix() : transformer.getPrefixes().getOrDefault(
            getName().toLowerCase(), getPrefix()
        );
    }
}

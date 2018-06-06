package com.avairebot.handlers;

import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlayerTransformer;

public class DatabaseEventHolder {

    private final GuildTransformer guild;
    private final PlayerTransformer player;

    public DatabaseEventHolder(GuildTransformer guild, PlayerTransformer player) {
        this.guild = guild;
        this.player = player;
    }

    public GuildTransformer getGuild() {
        return guild;
    }

    public PlayerTransformer getPlayer() {
        return player;
    }
}

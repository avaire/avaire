package com.avairebot.orion.contracts.commands.playlist;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.music.PlaylistCommand;
import com.avairebot.orion.database.collection.Collection;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.database.transformers.PlaylistTransformer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.core.entities.Message;

public abstract class PlaylistSubCommand {

    protected static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

    protected final Orion orion;
    protected final PlaylistCommand command;

    public PlaylistSubCommand(Orion orion, PlaylistCommand command) {
        this.orion = orion;
        this.command = command;
    }

    public final boolean onCommand(Message message, String[] args, GuildTransformer guild, Object object) {
        if (object instanceof PlaylistTransformer) {
            return onCommand(message, args, guild, (PlaylistTransformer) object);
        }

        return object instanceof Collection && onCommand(message, args, guild, (Collection) object);

    }

    public boolean onCommand(Message message, String[] args, GuildTransformer guild, PlaylistTransformer playlist) {
        return false;
    }

    public boolean onCommand(Message message, String[] args, GuildTransformer guild, Collection playlists) {
        return false;
    }
}

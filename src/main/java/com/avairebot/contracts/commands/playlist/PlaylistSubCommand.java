package com.avairebot.contracts.commands.playlist;

import com.avairebot.AvaIre;
import com.avairebot.commands.music.PlaylistCommand;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlaylistTransformer;
import net.dv8tion.jda.core.entities.Message;

public abstract class PlaylistSubCommand {

    protected final AvaIre avaire;
    protected final PlaylistCommand command;

    public PlaylistSubCommand(AvaIre avaire, PlaylistCommand command) {
        this.avaire = avaire;
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

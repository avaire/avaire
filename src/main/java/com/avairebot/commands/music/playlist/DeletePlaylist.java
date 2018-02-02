package com.avairebot.commands.music.playlist;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.cache.CacheType;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.music.PlaylistCommand;
import com.avairebot.contracts.commands.playlist.PlaylistSubCommand;
import com.avairebot.database.controllers.PlaylistController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlaylistTransformer;

import java.sql.SQLException;

public class DeletePlaylist extends PlaylistSubCommand {

    public DeletePlaylist(AvaIre avaire, PlaylistCommand command) {
        super(avaire, command);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args, GuildTransformer guild, PlaylistTransformer playlist) {
        try {
            avaire.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                .where("guild_id", context.getGuild().getId())
                .andWhere("id", playlist.getId())
                .delete();

            avaire.getCache().getAdapter(CacheType.MEMORY)
                .forget(PlaylistController.getCacheString(context.getGuild()));

            context.makeSuccess("The `:name` playlist has been deleted successfully!")
                .set("name", playlist.getName())
                .queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            context.makeError("Error: " + e.getMessage()).queue();
        }

        return false;
    }
}

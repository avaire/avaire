package com.avairebot.commands.music.playlist;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.music.PlaylistCommand;
import com.avairebot.contracts.commands.playlist.PlaylistSubCommand;
import com.avairebot.database.controllers.PlaylistController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlaylistTransformer;
import org.apache.commons.lang3.exception.ExceptionUtils;

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

            PlaylistController.forgetCache(context.getGuild().getIdLong());

            context.makeSuccess(context.i18n("playlistDeleted"))
                .set("name", playlist.getName())
                .queue();

            return true;
        } catch (SQLException e) {
            AvaIre.getLogger().error("SQLException on DeletePlaylist.onCommand \n", ExceptionUtils.getStackTrace(e));
            context.makeError("Error: " + e.getMessage()).queue();
        }

        return false;
    }
}

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

public class RemoveSongFromPlaylist extends PlaylistSubCommand {

    public RemoveSongFromPlaylist(AvaIre avaire, PlaylistCommand command) {
        super(avaire, command);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args, GuildTransformer guild, PlaylistTransformer playlist) {
        if (!isValidRequest(context, args, playlist)) {
            return false;
        }

        try {
            int id = Integer.parseInt(args[2], 10) - 1;
            if (id < 0 || id >= playlist.getSongs().size()) {
                context.makeWarning("Invalid id given, the number given is too :type\n`:command`")
                    .set("command", command.generateCommandTrigger(context.getMessage()) + " " + playlist.getName() + " removesong <song id>")
                    .set("type", id < 0 ? "low" : "high")
                    .queue();

                return false;
            }

            PlaylistTransformer.PlaylistSong removed = playlist.getSongs().remove(id);

            avaire.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                .where("id", playlist.getId()).andWhere("guild_id", context.getGuild().getId())
                .update(statement -> {
                    statement.set("songs", AvaIre.GSON.toJson(playlist.getSongs()), true);
                    statement.set("amount", playlist.getSize());
                });

            avaire.getCache().getAdapter(CacheType.MEMORY)
                .forget(PlaylistController.getCacheString(context.getGuild()));

            context.makeSuccess(":song has been successfully removed from the `:playlist` playlist")
                .set("song", String.format("[%s](%s)", removed.getTitle(), removed.getLink()))
                .set("playlist", playlist.getName())
                .queue();

            return true;
        } catch (NumberFormatException e) {
            context.makeWarning("Invalid id given, the id must be a number\n`:command`")
                .set("command", command.generateCommandTrigger(context.getMessage()) + " " + playlist.getName() + " removesong <song id>")
                .queue();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean isValidRequest(CommandMessage context, String[] args, PlaylistTransformer playlist) {
        if (args.length < 3) {
            context.makeWarning("Invalid format, missing the `song id` property!\n`:command`")
                .set("command", command.generateCommandTrigger(context.getMessage()) + " " + playlist.getName() + " removesong <song id>")
                .queue();

            return false;
        }

        if (playlist.getSongs().isEmpty()) {
            context.makeWarning("The `:playlist` playlist is already empty, there is nothing to remove.")
                .set("playlist", playlist.getName())
                .queue();

            return false;
        }

        return true;
    }
}

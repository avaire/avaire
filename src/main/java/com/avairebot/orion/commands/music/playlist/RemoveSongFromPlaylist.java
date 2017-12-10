package com.avairebot.orion.commands.music.playlist;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.music.PlaylistCommand;
import com.avairebot.orion.contracts.commands.playlist.PlaylistSubCommand;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.database.transformers.PlaylistTransformer;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.sql.SQLException;

public class RemoveSongFromPlaylist extends PlaylistSubCommand {

    public RemoveSongFromPlaylist(Orion orion, PlaylistCommand command) {
        super(orion, command);
    }

    @Override
    public boolean onCommand(Message message, String[] args, GuildTransformer guild, PlaylistTransformer playlist) {
        if (!isValidRequest(message, args, playlist)) {
            return false;
        }

        try {
            int id = Integer.parseInt(args[2], 10) - 1;
            if (id < 0 || id >= playlist.getSongs().size()) {
                MessageFactory.makeWarning(message, "Invalid id given, the number given is too :type\n`:command`")
                    .set("command", command.generateCommandTrigger(message) + " " + playlist.getName() + " removesong <song id>")
                    .set("type", id < 0 ? "low" : "high")
                    .queue();

                return false;
            }

            PlaylistTransformer.PlaylistSong removed = playlist.getSongs().remove(id);

            orion.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                .where("id", playlist.getId()).andWhere("guild_id", message.getGuild().getId())
                .update(statement -> {
                    statement.set("songs", GSON.toJson(playlist.getSongs()));
                    statement.set("size", playlist.getSongs().size());
                });

            MessageFactory.makeSuccess(message, ":song has been successfully removed from the `:playlist` playlist")
                .set("song", String.format("[%s](%s)", removed.getTitle(), removed.getLink()))
                .set("playlist", playlist.getName())
                .queue();

            return true;
        } catch (NumberFormatException e) {
            MessageFactory.makeWarning(message, "Invalid id given, the id must be a number\n`:command`")
                .set("command", command.generateCommandTrigger(message) + " " + playlist.getName() + " removesong <song id>")
                .queue();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean isValidRequest(Message message, String[] args, PlaylistTransformer playlist) {
        if (args.length < 3) {
            MessageFactory.makeWarning(message, "Invalid format, missing the `song id` property!\n`:command`")
                .set("command", command.generateCommandTrigger(message) + " " + playlist.getName() + " removesong <song id>")
                .queue();

            return false;
        }

        if (playlist.getSongs().isEmpty()) {
            MessageFactory.makeWarning(message, "The `:playlist` playlist is already empty, there is nothing to remove.")
                .set("playlist", playlist.getName())
                .queue();

            return false;
        }

        return true;
    }
}

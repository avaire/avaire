package com.avairebot.commands.music.playlist;

import com.avairebot.AvaIre;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.music.PlaylistCommand;
import com.avairebot.contracts.commands.playlist.PlaylistSubCommand;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SendPlaylists extends PlaylistSubCommand {

    public SendPlaylists(AvaIre avaire, PlaylistCommand command) {
        super(avaire, command);
    }

    @Override
    public boolean onCommand(Message message, String[] args, GuildTransformer guild, Collection playlists) {
        SimplePaginator paginator = new SimplePaginator(playlists.sort(
            Comparator.comparing(dataRow -> dataRow.getString("name"))
        ).getItems(), 5);

        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        List<String> messages = new ArrayList<>();
        paginator.forEach((index, key, val) -> {
            DataRow row = (DataRow) val;

            messages.add(String.format("%s\n       Playlist has **%s** song(s)",
                row.getString("name"), row.getInt("size")
            ));
        });

        String counter = String.format(" [ %s out of %s ]",
            playlists.size(), guild.getType().getLimits().getPlaylist().getPlaylists()
        );

        MessageFactory.makeInfo(message, "\u2022 " +
            String.join("\n\u2022 ", messages) + "\n\n" +
            paginator.generateFooter(command.generateCommandTrigger(message))
        ).setTitle(":musical_note: Music Playlist " + counter).queue();

        return true;
    }
}

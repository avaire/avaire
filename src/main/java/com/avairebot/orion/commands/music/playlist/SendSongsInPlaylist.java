package com.avairebot.orion.commands.music.playlist;

import com.avairebot.orion.Orion;
import com.avairebot.orion.chat.SimplePaginator;
import com.avairebot.orion.commands.music.PlaylistCommand;
import com.avairebot.orion.contracts.commands.playlist.PlaylistSubCommand;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.database.transformers.PlaylistTransformer;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.List;

public class SendSongsInPlaylist extends PlaylistSubCommand {

    public SendSongsInPlaylist(Orion orion, PlaylistCommand command) {
        super(orion, command);
    }

    @Override
    public boolean onCommand(Message message, String[] args, GuildTransformer guild, PlaylistTransformer playlist) {
        if (playlist.getSongs().isEmpty()) {
            MessageFactory.makeWarning(message, "There are no songs in this playlist, you can add songs to it by using the\n`:command` command.")
                .set("command", command.generateCommandTrigger(message) + " add <song url>")
                .queue();

            return false;
        }

        SimplePaginator paginator = new SimplePaginator(playlist.getSongs(), 10);
        if (args.length > 1) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[1], 1));
        }

        List<String> messages = new ArrayList<>();
        paginator.forEach((index, key, val) -> {
            PlaylistTransformer.PlaylistSong song = (PlaylistTransformer.PlaylistSong) val;

            messages.add(String.format("`%s` [%s](%s) [%s]",
                index + 1,
                song.getTitle(),
                song.getLink(),
                song.getDuration()
            ));
        });

        MessageFactory.makeInfo(message,
            String.join("\n", messages) + "\n\n" + paginator.generateFooter(command.generateCommandTrigger(message))
        ).setTitle(":musical_note: " + playlist.getName()).queue();

        return true;
    }
}

package com.avairebot.commands.music.playlist;

import com.avairebot.AvaIre;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.music.PlaylistCommand;
import com.avairebot.contracts.commands.playlist.PlaylistSubCommand;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlaylistTransformer;
import com.avairebot.utilities.NumberUtil;

import java.util.ArrayList;
import java.util.List;

public class SendSongsInPlaylist extends PlaylistSubCommand {

    public SendSongsInPlaylist(AvaIre avaire, PlaylistCommand command) {
        super(avaire, command);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args, GuildTransformer guild, PlaylistTransformer playlist) {
        if (playlist.getSongs().isEmpty()) {
            context.makeWarning(context.i18n("playlistIsEmpty"))
                .set("command", command.generateCommandTrigger(context.getMessage()) + " " + playlist.getName() + " add <song url>")
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

            messages.add(context.i18n("playlistSongLine",
                index + 1,
                song.getTitle(),
                song.getLink(),
                song.getDuration()
            ));
        });

        context.makeInfo(
            String.join("\n", messages) + "\n\n" + paginator.generateFooter(command.generateCommandTrigger(context.getMessage()) + " " + playlist.getName())
        ).setTitle(":musical_note: " + playlist.getName()).queue();

        return true;
    }
}

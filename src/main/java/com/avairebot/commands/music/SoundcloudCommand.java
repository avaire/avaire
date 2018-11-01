/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.commands.CommandContainer;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.utilities.NumberUtil;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SoundcloudCommand extends Command {

    public SoundcloudCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "SoundCloud Command";
    }

    @Override
    public String getDescription() {
        return "Plays the provided song for you, if just the song title is given the bot will search SoundCloud for your song and give you some suggestions, you can also use YouTube, SoundCloud, TwitchTV, Bandcamp, and Vimeo link, or raw sound file, mp3, flac, wav, webm, mp4, ogg, aac, m3u and pls formats.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <song>` - Plays the given song");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(PlayCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("soundcloud", "sc");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:none",
            "throttle:guild,2,5",
            "musicChannel"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command A cool song` - Finds songs with the name \"A cool song\".",
            "`:command https://soundcloud.com/yellowclaw/yellow-claw-flux-pavilion-catch-me-feat-naaz` - Plays the song off a link"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MUSIC_START_PLAYING);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingMusicQueue");
        }

        CommandContainer container = CommandHandler.getCommand(PlayCommand.class);
        PlayCommand playCommand = (PlayCommand) container.getCommand();

        context.setI18nCommandPrefix(container);

        if (AudioHandler.getDefaultAudioHandler().hasAudioSession(context) && NumberUtil.isNumeric(args[0])) {
            return playCommand.loadSongFromSession(context, args);
        }

        try {
            new URL(String.join(" ", args));

            return container.getCommand().onCommand(context, args);
        } catch (MalformedURLException ex) {
            return container.getCommand().onCommand(context, new String[]{
                "scsearch:", String.join(" ", args)
            });
        }
    }
}

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
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.shared.DiscordConstants;
import com.avairebot.utilities.NumberUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SetDefaultVolumeCommand extends Command {

    public SetDefaultVolumeCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Set Default Volume Command";
    }

    @Override
    public String getDescription(@Nullable CommandContext context) {
        String prefix = context != null && context.isGuildMessage()
            ? generateCommandPrefix(context.getMessage())
            : DiscordConstants.DEFAULT_COMMAND_PREFIX;

        return String.format(String.join("\n", Arrays.asList(
            "Sets the default volume that the music should play at when Ava first joins a voice channel.",
            "**Note:** This does not change the volume of music already playing, to change that, use the `%svolume` command instead."
        )), prefix);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("default-volume", "set-volume");
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Displays the current default volume",
            "`:command <volume>` - Changes the default volume to the given volume."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "`:command 75` - Sets the default volume to 75"
        );
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:normal",
            "throttle:guild,1,4",
            "hasVoted",
            "musicChannel"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MUSIC_SETTINGS);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "default volume");
        }

        if (args.length == 0) {
            return sendCurrentVolume(context, transformer);
        }

        int vol = NumberUtil.parseInt(args[0], 0);
        if (vol < 1 || vol > 100) {
            return sendErrorMessage(context, context.i18n("mustBeNumber"));
        }

        transformer.setDefaultVolume(vol);

        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> statement.set("default_volume", vol));

            context.makeSuccess(context.i18n("changedVolume"))
                .set("volume", vol)
                .queue();

            return true;
        } catch (SQLException e) {
            AvaIre.getLogger().error("Failed to store the default volume in the database due to a SQLException: ", e);
            context.makeError(context.i18n("failedToSave", e.getMessage())).queue();
        }

        return false;
    }

    private boolean sendCurrentVolume(CommandMessage context, GuildTransformer transformer) {
        context.makeSuccess(context.i18n("currentVolume"))
            .set("volume", transformer.getDefaultVolume())
            .queue();

        return false;
    }
}

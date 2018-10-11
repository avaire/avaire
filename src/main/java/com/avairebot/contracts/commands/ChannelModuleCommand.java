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

package com.avairebot.contracts.commands;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.StringReplacementUtil;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public abstract class ChannelModuleCommand extends Command {

    private static final Pattern colorRegEx = Pattern.compile("^[0-9A-F]{6}$");

    public ChannelModuleCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:channel,1,5"
        );
    }

    protected boolean handleEmbedOption(CommandMessage context, String[] args, GuildTransformer guildTransformer, ChannelTransformer channelTransformer, Supplier<Boolean> callback) {
        getChannelModule(channelTransformer).setEmbedColor(null);

        if (args.length > 1) {
            String color = args[1].toUpperCase();
            if (color.startsWith("#")) {
                color = color.substring(1);
            }

            if (!colorRegEx.matcher(color).matches()) {
                return sendErrorMessage(context, context.i18nRaw("administration.channelModule.invalidColorGiven"));
            }

            getChannelModule(channelTransformer).setEmbedColor("#" + color);
        }

        return updateDatabase(context, guildTransformer, callback);
    }

    protected boolean updateDatabase(CommandMessage context, GuildTransformer guildTransformer, Supplier<Boolean> callback) {
        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .andWhere("id", context.getGuild().getId())
                .update(statement -> statement.set("channels", guildTransformer.channelsToJson(), true));

            return callback.get();
        } catch (SQLException ex) {
            AvaIre.getLogger().error(ex.getMessage(), ex);

            context.makeError("Failed to save the guild settings: " + ex.getMessage()).queue();
            return false;
        }
    }

    protected boolean sendExampleMessage(CommandMessage context, User user, ChannelTransformer transformer, String defaultValue) {
        String message = StringReplacementUtil.parse(
            context.getGuild(), context.getChannel(), user,
            getChannelModule(transformer).getMessage() == null ?
                defaultValue : getChannelModule(transformer).getMessage()
        );

        String embedColor = getChannelModule(transformer).getEmbedColor();
        if (embedColor == null) {
            context.getMessageChannel().sendMessage(message).queue();
            return true;
        }

        context.getMessageChannel().sendMessage(
            MessageFactory.createEmbeddedBuilder()
                .setDescription(message)
                .setColor(Color.decode(embedColor))
                .build()
        ).queue();

        return true;
    }

    protected boolean sendEnableMessage(CommandMessage context, ChannelTransformer channelTransformer, String type) {
        context.makeSuccess(context.i18nRaw("administration.channelModule.message"))
            .set("type", type)
            .set("message", getChannelModule(channelTransformer).getMessage())
            .set("command", generateCommandTrigger(context.getMessage()))
            .queue();

        return true;
    }

    public abstract ChannelTransformer.MessageModule getChannelModule(ChannelTransformer transformer);
}

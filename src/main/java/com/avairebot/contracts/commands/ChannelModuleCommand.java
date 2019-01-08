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

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public abstract class ChannelModuleCommand extends Command {

    private static final Pattern colorRegEx = Pattern.compile("^[0-9A-F]{6}$");

    /**
     * Creates a new channel module instance that won't work in DMs.
     *
     * @param avaire The main {@link AvaIre avaire} application instance.
     */
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

    /**
     * Handles the embed option for the channel module, this method is called
     * when the command specifies that it should use or disable embed
     * messages, by either parsing a colour code in a HEX format to
     * set the embed colour, or when using it with no additional
     * arguments to disable the feature.
     *
     * @param context            The command message context.
     * @param args               The arguments passed to the original command.
     * @param guildTransformer   The guild database transformer representing the current guild.
     * @param channelTransformer The channel database transformer representing the current channel.
     * @param callback           The callback that should be invoked when the database record is updated.
     * @return {@code True} if everything ran successfully, or {@code False} if an error occurred, or invalid arguments was given.
     */
    protected boolean handleEmbedOption(
        CommandMessage context,
        String[] args,
        GuildTransformer guildTransformer,
        ChannelTransformer channelTransformer,
        Supplier<Boolean> callback
    ) {
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

    /**
     * Updates the database for the current guild, and
     * then calls the callback supplier on success.
     *
     * @param context          The command message context.
     * @param guildTransformer The guild database transformer representing the current guild.
     * @param callback         The callback that should be invoked when the database record is updated.
     * @return The result of the {@code callback} if the database record was
     * updated successfully, or {@code False} if an error occurred.
     */
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

    /**
     * Sends the example message of the command module by using
     * the provided command context, channel transformer,
     * and default value, sending it to the given user.
     *
     * @param context      The command message context.
     * @param user         The user that should be used for the user placeholders.
     * @param transformer  The channel database transformer representing the current channel.
     * @param defaultValue The default value that should be displayed if the
     *                     guild doesn't have one set for the module.
     * @return Always returns {@code True}.
     */
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

    /**
     * Sends the "Module has been enabled" message for the
     * given channel transformer, and the given type.
     *
     * @param context            The command message context.
     * @param channelTransformer The channel database transformer representing the current channel.
     * @param type               The of enabled message that should be sent.
     * @return Always returns {@code True}.
     */
    protected boolean sendEnableMessage(CommandMessage context, ChannelTransformer channelTransformer, String type) {
        context.makeSuccess(context.i18nRaw("administration.channelModule.message"))
            .set("type", type)
            .set("message", getChannelModule(channelTransformer).getMessage())
            .set("command", generateCommandTrigger(context.getMessage()))
            .queue();

        return true;
    }

    /**
     * Returns the channel module that should be
     * used for the channel module instance.
     *
     * @param transformer The channel transformer the module should be pulled from.
     * @return The {@link ChannelTransformer.MessageModule channel message module}
     * that should be used for the channel module instance.
     */
    @Nonnull
    public abstract ChannelTransformer.MessageModule getChannelModule(ChannelTransformer transformer);
}

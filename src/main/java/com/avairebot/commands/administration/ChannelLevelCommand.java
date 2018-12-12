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

package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.utility.RankCommand;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.ComparatorUtil;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChannelLevelCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(ChannelLevelCommand.class);

    public ChannelLevelCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Channel Level Command";
    }

    @Override
    public String getDescription() {
        return "Toggles XP rewards on or off for the mentioned channel, if no arguments is given the channels that currently has their channel rewards disabled will be displayed instead, the command can be used to prevent rewarding users experience in certain channels like #spam channels.\n**Note** this command requires that the leveling system is enabled for the server.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <channel> [status]` - Toggles the level feature on/off.",
            "`:command` - Lists channels with their XP status disabled."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command #spam off` - Disables XP gains in the #spam channel.",
            "`:command #sandbox` - Toggles the XP gains on/off for the #sandbox channel.",
            "`:command` - Lists all the channels that currently has their XP status disabled."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            LevelCommand.class,
            LevelHierarchyCommand.class,
            LevelModifierCommand.class,
            LevelAlertsCommand.class,
            RankCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("channellevel", "clvl");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:user,1,5"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.LEVEL_AND_EXPERIENCE);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null || !guildTransformer.isLevels()) {
            return sendErrorMessage(
                context,
                "errors.requireLevelFeatureToBeEnabled",
                CommandHandler.getCommand(LevelCommand.class)
                    .getCommand().generateCommandTrigger(context.getMessage())
            );
        }

        if (args.length == 0 || NumberUtil.parseInt(args[0], -1) > 0) {
            return sendDisabledChannels(context, guildTransformer);
        }

        Channel channel = MentionableUtil.getChannel(context.getMessage(), args);
        if (channel == null || !(channel instanceof TextChannel)) {
            return sendErrorMessage(context, context.i18n("invalidChannel"));
        }

        TextChannel textChannel = (TextChannel) channel;

        if (!textChannel.canTalk()) {
            return sendErrorMessage(context, context.i18n("cantTalkInChannel",
                textChannel.getAsMention()
            ));
        }

        if (args.length > 1) {
            return handleToggleChannel(context, textChannel, ComparatorUtil.getFuzzyType(args[1]));
        }
        return handleToggleChannel(context, textChannel, ComparatorUtil.ComparatorType.UNKNOWN);
    }

    private boolean sendDisabledChannels(CommandMessage context, GuildTransformer guildTransformer) {
        if (guildTransformer.getLevelExemptChannels().isEmpty()) {
            return sendErrorMessage(context, context.i18n("noChannelsWithRewardsDisabled"),
                generateCommandTrigger(context.getMessage())
            );
        }

        List<String> channels = new ArrayList<>();
        for (Long channelId : guildTransformer.getLevelExemptChannels()) {
            TextChannel textChannel = context.getGuild().getTextChannelById(channelId);
            if (textChannel != null) {
                channels.add(textChannel.getAsMention());
            }
        }

        context.makeInfo(context.i18n("listChannels"))
            .set("channels", String.join(", ", channels))
            .setTitle(context.i18n("listChannelsTitle",
                guildTransformer.getLevelExemptChannels().size()
            ))
            .queue();

        return true;
    }

    @SuppressWarnings("ConstantConditions")
    private boolean handleToggleChannel(CommandMessage context, TextChannel channel, ComparatorUtil.ComparatorType value) {
        GuildTransformer guildTransformer = context.getGuildTransformer();

        switch (value) {
            case TRUE:
                guildTransformer.getLevelExemptChannels().remove(channel.getIdLong());
                break;

            case FALSE:
                guildTransformer.getLevelExemptChannels().add(channel.getIdLong());
                break;

            case UNKNOWN:
                if (guildTransformer.getLevelExemptChannels().contains(channel.getIdLong())) {
                    guildTransformer.getLevelExemptChannels().remove(channel.getIdLong());
                } else {
                    guildTransformer.getLevelExemptChannels().add(channel.getIdLong());
                }
                break;
        }

        boolean isEnabled = !guildTransformer.getLevelExemptChannels().contains(channel.getIdLong());

        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> {
                    statement.set("level_exempt_channels", AvaIre.gson.toJson(
                        guildTransformer.getLevelExemptChannels()
                    ), true);
                });

            context.makeSuccess(context.i18n("success"))
                .set("channel", channel.getAsMention())
                .set("status", context.i18n(isEnabled ? "status.enabled" : "status.disabled"))
                .queue();

            return true;
        } catch (SQLException e) {
            log.error("Failed to save the level exempt channels to the data for guild {}, error: {}",
                context.getGuild().getId(), e.getMessage(), e
            );

            context.makeError(context.i18n("failedToUpdate")).queue();

            return false;
        }
    }
}

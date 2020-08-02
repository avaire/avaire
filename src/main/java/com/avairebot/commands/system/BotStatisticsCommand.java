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

package com.avairebot.commands.system;

import com.avairebot.AppInfo;
import com.avairebot.AvaIre;

import com.avairebot.chat.MessageType;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.commands.utility.UptimeCommand;
import com.avairebot.contracts.commands.SystemCommand;
import com.avairebot.language.I18n;
import com.avairebot.metrics.Metrics;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;


import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BotStatisticsCommand extends SystemCommand {

    public BotStatisticsCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Bot Statistics Command";
    }

    @Override
    public String getDescription() {
        return "Displays information about the current state of the bot, this command can be pretty heavy to run since a lot of calculations are being run to get some of the information.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Shows some stats about the bot.");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("statistics", "stats");
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.SYSTEM_ROLE;
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        context.makeEmbeddedMessage(MessageType.INFO,
            new MessageEmbed.Field("Database Stats", I18n.format("**{0}** Selects\n**{1}** Inserts",
                NumberUtil.formatNicely(Metrics.databaseQueries.labels("SELECT").get()),
                NumberUtil.formatNicely(Metrics.databaseQueries.labels("INSERT").get())
            ), true),
            new MessageEmbed.Field(EmbedBuilder.ZERO_WIDTH_SPACE, I18n.format("**{0}** Updates\n**{1}** Deletes",
                NumberUtil.formatNicely(Metrics.databaseQueries.labels("UPDATE").get()),
                NumberUtil.formatNicely(Metrics.databaseQueries.labels("DELETE").get())
            ), true),
            new MessageEmbed.Field("Discord Shards", I18n.format("**{0}** Shards\n**{1} ms** Average Ping",
                avaire.getShardManager().getShards().size(),
                NumberUtil.formatNicely(avaire.getShardManager().getAverageGatewayPing())
            ), true),
            new MessageEmbed.Field("Member Stats", I18n.format("**{0}** Unique Users\n**{1}** Unique Bots",
                NumberUtil.formatNicely(avaire.getShardManager().getUsers().stream().filter(user -> !user.isBot()).count()),
                NumberUtil.formatNicely(avaire.getShardManager().getUsers().stream().filter(User::isBot).count())
            ), true),
            new MessageEmbed.Field("Channel Stats", I18n.format("**{0}** Text Channels\n**{1}** Voice Channels",
                NumberUtil.formatNicely(avaire.getShardEntityCounter().getTextChannels()),
                NumberUtil.formatNicely(avaire.getShardEntityCounter().getVoiceChannels())
            ), true),
            new MessageEmbed.Field("Guild Stats", I18n.format("**{0}** Guilds in Shard\n**{1}** Total Guilds",
                NumberUtil.formatNicely(context.getJDA().getGuilds().size()),
                NumberUtil.formatNicely(avaire.getShardManager().getGuilds().size())
            ), true)
        )
            .setAuthor("AvaIre v" + AppInfo.getAppInfo().version, "https://avairebot.com/support", avaire.getSelfUser().getEffectiveAvatarUrl())
            .setDescription("The bot has been online for " + formatUptimeNicely())
            .queue();

        return false;
    }

    @SuppressWarnings("ConstantConditions")
    private String formatUptimeNicely() {
        return ((UptimeCommand) CommandHandler.getCommand(UptimeCommand.class).getCommand())
            .formatUptimeNicely((int) ManagementFactory.getRuntimeMXBean().getUptime() / 1000);
    }
}

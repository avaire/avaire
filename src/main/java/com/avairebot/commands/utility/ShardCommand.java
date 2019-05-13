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

package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandContainer;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.SelfUser;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShardCommand extends Command {

    public ShardCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Shard Command";
    }

    @Override
    public String getDescription() {
        // If the shard manager is null, we're still starting up the bot.
        if (avaire.getShardManager() == null) {
            return "If you're seeing this message, contact one of the bot developers.";
        }

        return "Displays the status of all the shards for the bot, including their server count, channel count, user count and latency."
            + ((avaire.getShardManager().getShards().size() < 2) ? "\n**Shards are currently disabled: This command will just run the stats command.**" : "");
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command [page]` Displays the shard information, with 12 shards per page.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command 2` - Displays the 2nd page of shard information.");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("shards", "shard");
    }

    @Override
    public CommandPriority getCommandPriority() {
        if (avaire.getShardManager().getShards().size() < 2) {
            return CommandPriority.HIDDEN;
        }
        return super.getCommandPriority();
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.BOT_INFORMATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (avaire.getSettings().getShardCount() < 2) {
            CommandContainer container = CommandHandler.getCommand(StatsCommand.class);
            if (container == null) {
                return sendErrorMessage(context, "Sharding is not enabled right now :(");
            }
            context.setI18nCommandPrefix(container);
            return container.getCommand().onCommand(context, args);
        }

        return handleCommand(context, args);
    }

    private boolean handleCommand(CommandMessage context, String[] args) {
        int currentShardId = context.isGuildMessage()
            ? context.getJDA().getShardInfo().getShardId()
            : 0;

        List<MessageEmbed.Field> shards = new ArrayList<>();
        for (int i = avaire.getSettings().getShardCount() - 1; i >= 0; i--) {
            JDA shard = avaire.getShardManager().getShardById(i);

            if (shard == null) {
                shards.add(new MessageEmbed.Field(String.format("Shard #%s %s",
                    i, getShardConnectionIcon(JDA.Status.SHUTDOWN)
                ), "Connecting to Discord...", true));
                continue;
            }

            if (!shard.getStatus().equals(JDA.Status.CONNECTED)) {
                shards.add(new MessageEmbed.Field(String.format("Shard #%s %s",
                    i, getShardConnectionIcon(shard.getStatus())
                ), formatShardStatus(shard.getStatus()), true));
                continue;
            }

            shards.add(new MessageEmbed.Field(String.format("Shard #%s %s%s",
                shard.getShardInfo().getShardId(),
                getShardConnectionIcon(shard.getStatus()),
                currentShardId == shard.getShardInfo().getShardId() ? "\uD83D\uDCCC" : ""
            ), String.format("%s users\n%s guilds\n%s ms ping",
                NumberUtil.formatNicely(shard.getUsers().size()),
                NumberUtil.formatNicely(shard.getGuilds().size()),
                NumberUtil.formatNicely(shard.getPing())
            ), true));
        }

        SimplePaginator<MessageEmbed.Field> paginator = new SimplePaginator<>(shards, 12);
        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        PlaceholderMessage message = context.makeEmbeddedMessage()
            .requestedBy(context)
            .setDescription("Currently serving **:users** users in **:channels** channels, and **:servers** servers.")
            .set("servers", NumberUtil.formatNicely(avaire.getShardEntityCounter().getGuilds()))
            .set("channels", NumberUtil.formatNicely(avaire.getShardEntityCounter().getChannels()))
            .set("users", NumberUtil.formatNicely(avaire.getShardEntityCounter().getUsers()));

        paginator.forEach((index, key, val) -> message.addField(val));
        message.addField("", paginator.generateFooter(context.getGuild(), generateCommandTrigger(context.getMessage())), false);

        SelfUser selfUser = avaire.getSelfUser();
        message.setAuthor(
            "Shard Information",
            "http://status.avairebot.com/",
            selfUser == null ? null : selfUser.getEffectiveAvatarUrl()
        ).queue();

        return true;
    }

    private String formatShardStatus(JDA.Status status) {
        final String name = status.name().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private String getShardConnectionIcon(JDA.Status status) {
        switch (status) {
            case CONNECTED:
                return Constants.EMOTE_ONLINE;

            case FAILED_TO_LOGIN:
            case DISCONNECTED:
            case SHUTTING_DOWN:
            case SHUTDOWN:
                return Constants.EMOTE_DND;

            default:
                return Constants.EMOTE_AWAY;
        }
    }
}

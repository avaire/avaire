/*
 * Copyright (c) 2019.
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
import com.avairebot.changelog.ChangelogHandler;
import com.avairebot.changelog.ChangelogMessage;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.shared.DiscordConstants;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChangelogCommand extends Command {

    public ChangelogCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Changelog Command";
    }

    @Override
    public String getDescription() {
        return "Displays a log of changes made to the bot between different versions, versions can be listed by themselves, and changes for the given versions can be looked up through this command, allowing users to see what happened, and when it happened.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command list [page]` - Lists all the versions that has a changelog message.",
            "`:command <version>` - Displays the changelog for the given version.",
            "`:command` - Displays the latest changelog + The last 10 versions."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command 0.9.78`",
            "`:command list 2`"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("changelog", "changes");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        TextChannel changelogChannel = avaire.getShardManager().getTextChannelById(DiscordConstants.CHANGELOG_CHANNEL_ID);
        if (changelogChannel == null) {
            return sendErrorMessage(context, context.i18n("invalidChangelogChannel"));
        }

        if (!canReadChangelog(changelogChannel)) {
            return sendErrorMessage(context, context.i18n("cantReadChangelogChannel"));
        }

        if (!ChangelogHandler.hasLoadedMessages()) {
            context.getChannel().sendTyping().queue();
        }

        ChangelogHandler.loadAndGetMessages(messages -> {
            if (args.length == 0) {
                displayLatestLog(context, messages);
                return;
            }

            if (args[0].equals("list")) {
                displayListOfLogs(context, messages, Arrays.copyOfRange(
                    args, 1, args.length
                ));
                return;
            }

            String version = args[0];
            if (!version.startsWith("v")) {
                version = "v" + version;
            }

            ChangelogMessage changelogMessage = getChangelogMessageFromVersion(messages, version);
            if (changelogMessage == null) {
                sendErrorMessage(context, context.i18n("invalidVersionGiven", args[0]));
                return;
            }

            context.makeInfo(changelogMessage.getMessage())
                .setTitle(changelogMessage.getVersion())
                .setFooter(context.i18n("latestVersion", messages.get(0).getVersion()))
                .queue();
        });

        return true;
    }

    private void displayLatestLog(CommandMessage context, List<ChangelogMessage> messages) {
        if (messages.isEmpty()) {
            context.makeInfo(context.i18n("noChangelogMessages")).queue();
            return;
        }

        List<String> otherVersions = new ArrayList<>();
        ChangelogMessage latestMessage = messages.get(0);

        for (ChangelogMessage changelogMessage : messages) {
            if (latestMessage.getMessageId() == changelogMessage.getMessageId()) {
                continue;
            }
            otherVersions.add(changelogMessage.getVersion());
        }

        PlaceholderMessage message = context.makeInfo(latestMessage.getMessage())
            .setTitle(latestMessage.getVersion())
            .setTimestamp(latestMessage.getCreatedAt().getTime().toInstant());

        if (!otherVersions.isEmpty()) {
            message.addField(context.i18n("lastFewVersions"), String.join(", ", otherVersions), false);
        }

        message.queue();
    }

    private void displayListOfLogs(CommandMessage context, List<ChangelogMessage> messages, String[] args) {
        SimplePaginator<ChangelogMessage> paginator = new SimplePaginator<>(messages, 50);
        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        List<String> strings = new ArrayList<>();
        paginator.forEach((index, key, message) -> strings.add(message.getVersion()));

        context.makeInfo(String.join(", ", strings) + "\n\n" +
            paginator.generateFooter(context.getGuild(), generateCommandTrigger(context.getMessage()) + " list")
        ).setTitle(context.i18n("changelogVersions", paginator.getPages())).queue();
    }

    @Nullable
    private ChangelogMessage getChangelogMessageFromVersion(List<ChangelogMessage> messages, String version) {
        for (ChangelogMessage message : messages) {
            if (message.getVersion().equals(version)) {
                return message;
            }
        }
        return null;
    }

    private boolean canReadChangelog(TextChannel changelogChannel) {
        return changelogChannel.getGuild().getSelfMember().hasPermission(
            changelogChannel,
            Permission.MESSAGE_READ,
            Permission.MESSAGE_HISTORY
        );
    }
}

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

package com.avairebot.changelog;

import com.avairebot.AvaIre;
import com.avairebot.contracts.changelog.ChangelogLoader;
import com.avairebot.shared.DiscordConstants;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.*;
import java.util.stream.Collectors;

public class ChangelogHandler {

    private static final LinkedHashMap<Long, ChangelogMessage> messages = new LinkedHashMap<>();

    public static boolean hasLoadedMessages() {
        return !messages.isEmpty();
    }

    public static LinkedHashMap<Long, ChangelogMessage> getMessagesMap() {
        return messages;
    }

    public static Collection<ChangelogMessage> getMessages() {
        return messages.entrySet().parallelStream()
            .sorted(Collections.reverseOrder(Comparator.comparingLong(Map.Entry::getKey)))
            .collect(Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue,
                (e1, e2) -> e2, LinkedHashMap::new
            )).values();
    }

    public synchronized static void loadAndGetMessages(ChangelogLoader loader) {
        if (!messages.isEmpty()) {
            loader.handle(sortAndFormatMessages(messages.values()));
            return;
        }

        TextChannel changelogChannel = AvaIre.getInstance().getShardManager().getTextChannelById(DiscordConstants.CHANGELOG_CHANNEL_ID);
        if (changelogChannel == null) {
            loader.handle(sortAndFormatMessages(messages.values()));
            return;
        }

        loadHistoryMessages(changelogChannel.getHistory(), loader);
    }

    private static void loadHistoryMessages(MessageHistory history, ChangelogLoader loader) {
        history.retrievePast(100).queue(loadedMessages -> {
            for (Message message : loadedMessages) {
                messages.put(message.getIdLong(), new ChangelogMessage(message));
            }

            if (loadedMessages.size() == 100) {
                loadHistoryMessages(history, loader);
                return;
            }

            loader.handle(sortAndFormatMessages(messages.values()));
        });
    }

    private static List<ChangelogMessage> sortAndFormatMessages(Collection<ChangelogMessage> messages) {
        List<ChangelogMessage> changelogMessages = new ArrayList<>(messages);

        changelogMessages.sort(Comparator.comparingLong(ChangelogMessage::getMessageId));
        Collections.reverse(changelogMessages);

        return changelogMessages;
    }
}

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

import com.avairebot.time.Carbon;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.regex.Pattern;

public class ChangelogMessage {

    private static final Pattern mentionRegex = Pattern.compile(
        "<+[@|!|#|&]{1,2}[0-9]{16,}>"
    );

    private final long messageId;
    private final String version;
    private final String message;
    private final Carbon createdAt;
    private final int features;

    public ChangelogMessage(Message message) {
        this.messageId = message.getIdLong();

        String content = message.getContentRaw();
        String[] parts = content.split("\n");

        this.createdAt = Carbon.createFromOffsetDateTime(message.getCreationTime());
        this.version = parts[0].toLowerCase().replaceAll("\\*", "");

        boolean hasMentions = mentionRegex.matcher(parts[parts.length - 1]).find();

        parts = Arrays.copyOfRange(
            parts, 1, parts.length - (hasMentions ? 1 : 0)
        );

        this.message = String.join("\n", parts);

        int features = 0;
        for (String part : parts) {
            if (part.trim().startsWith("-")) {
                features++;
            }
        }
        this.features = features;
    }

    public long getMessageId() {
        return messageId;
    }

    public String getVersion() {
        return version;
    }

    public String getMessage() {
        return message;
    }

    public Carbon getCreatedAt() {
        return createdAt;
    }

    public int getFeatures() {
        return features;
    }
}

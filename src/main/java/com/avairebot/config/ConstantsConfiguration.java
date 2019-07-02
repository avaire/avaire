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

package com.avairebot.config;

import com.avairebot.AvaIre;
import com.avairebot.shared.DiscordConstants;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;

public class ConstantsConfiguration extends Configuration {

    private final HashMap<String, Long> cache = new HashMap<>();

    public ConstantsConfiguration(AvaIre plugin) throws IOException {
        super(plugin, null, "constants.yml");
    }

    public long getFeedbackChannelId() {
        return loadProperty("feedback-channel", DiscordConstants.FEEDBACK_CHANNEL_ID);
    }

    public long getChangelogChannelId() {
        return loadProperty("changelog-channel", DiscordConstants.CHANGELOG_CHANNEL_ID);
    }

    public long getActivityLogChannelId() {
        return loadProperty("activity-log-channel", DiscordConstants.ACTIVITY_LOG_CHANNEL_ID);
    }

    public long getBotAdminExceptionRoleId() {
        return loadProperty("bot-admin-exception-role", DiscordConstants.BOT_ADMIN_EXCEPTION_ROLE);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        cache.clear();
    }

    private long loadProperty(@Nonnull String key, long fallback) {
        if (!cache.containsKey(key)) {
            long val = getLong(key, fallback);
            cache.put(key, val > 0 ? val : fallback);
        }
        return cache.get(key);
    }
}

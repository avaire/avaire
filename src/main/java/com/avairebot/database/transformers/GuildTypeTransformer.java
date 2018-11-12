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

package com.avairebot.database.transformers;

import com.avairebot.AvaIre;
import com.avairebot.contracts.database.transformers.Transformer;
import com.avairebot.contracts.debug.Evalable;
import com.avairebot.database.collection.DataRow;

public class GuildTypeTransformer extends Transformer {

    private static final String defaultName = "Default";

    protected String name = defaultName;

    GuildTypeLimits limits = new GuildTypeLimits();

    GuildTypeTransformer() {
        super(null);
    }

    GuildTypeTransformer(DataRow data) {
        super(data);

        if (hasData()) {
            if (data.getString("type_name", null) != null) {
                name = data.getString("type_name");
            }

            if (data.getString("type_limits", null) != null) {
                GuildTypeLimits typeLimits = AvaIre.gson.fromJson(data.getString("type_limits"), GuildTypeLimits.class);
                if (typeLimits != null) {
                    if (typeLimits.levelRoles < limits.levelRoles) {
                        typeLimits.levelRoles = limits.levelRoles;
                    }

                    if (typeLimits.selfAssignableRoles < limits.selfAssignableRoles) {
                        typeLimits.selfAssignableRoles = limits.selfAssignableRoles;
                    }

                    limits = typeLimits;
                }
            }
        }
    }

    public boolean isDefault() {
        return getName().equals(defaultName);
    }

    public String getName() {
        return name;
    }

    public GuildTypeLimits getLimits() {
        return limits;
    }

    public class GuildTypeLimits extends Evalable {

        protected GuildReactionRoles reactionRoles = new GuildReactionRoles();
        protected GuildTypePlaylist playlist = new GuildTypePlaylist();

        int aliases = 20;
        int selfAssignableRoles = 15;
        int levelRoles = 10;

        public int getAliases() {
            return aliases;
        }

        public int getSelfAssignableRoles() {
            return selfAssignableRoles;
        }

        public int getLevelRoles() {
            return levelRoles;
        }

        public GuildTypePlaylist getPlaylist() {
            return playlist;
        }

        public GuildReactionRoles getReactionRoles() {
            return reactionRoles;
        }

        public class GuildTypePlaylist extends Evalable {

            int lists = 5;
            int songs = 30;

            public int getPlaylists() {
                return lists;
            }

            public int getSongs() {
                return songs;
            }
        }

        public class GuildReactionRoles extends Evalable {

            int messages = 3;
            int rolesPerMessage = 5;

            public int getMessages() {
                return messages;
            }

            public int getRolesPerMessage() {
                return rolesPerMessage;
            }
        }
    }
}

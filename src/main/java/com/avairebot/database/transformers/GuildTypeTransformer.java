package com.avairebot.database.transformers;

import com.avairebot.AvaIre;
import com.avairebot.contracts.database.transformers.Transformer;
import com.avairebot.database.collection.DataRow;

public class GuildTypeTransformer extends Transformer {

    private static final String defaultName = "Default";

    private String name = defaultName;
    private GuildTypeLimits limits = new GuildTypeLimits();

    public GuildTypeTransformer(DataRow data) {
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

    public class GuildTypeLimits {

        private GuildTypePlaylist playlist = new GuildTypePlaylist();
        private int aliases = 20;
        private int selfAssignableRoles = 15;
        private int levelRoles = 10;

        public int getAliases() {
            return aliases;
        }

        public int getSelfAssignableRoles() {
            return selfAssignableRoles;
        }

        public GuildTypePlaylist getPlaylist() {
            return playlist;
        }

        public int getLevelRoles() {
            return levelRoles;
        }

        public class GuildTypePlaylist {

            private int lists = 5;
            private int songs = 30;

            public int getPlaylists() {
                return lists;
            }

            public int getSongs() {
                return songs;
            }
        }
    }
}

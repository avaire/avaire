package com.avairebot.database.transformers;

import com.avairebot.AvaIre;
import com.avairebot.contracts.database.transformers.Transformer;
import com.avairebot.database.collection.DataRow;

public class GuildTypeTransformer extends Transformer {

    private static final String DEFAULT_NAME = "Default";

    private String name = GuildTypeTransformer.DEFAULT_NAME;
    private GuildTypeLimits limits = new GuildTypeLimits();

    public GuildTypeTransformer(DataRow data) {
        super(data);

        if (hasData()) {
            if (data.getString("type_name", null) != null) {
                name = data.getString("type_name");
            }

            if (data.getString("type_limits", null) != null) {
                GuildTypeLimits typeLimits = AvaIre.GSON.fromJson(data.getString("type_limits"), GuildTypeLimits.class);
                if (typeLimits != null) {
                    limits = typeLimits;
                }
            }
        }
    }

    public boolean isDefault() {
        return getName().equals(DEFAULT_NAME);
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

        public int getAliases() {
            return aliases;
        }

        public int getSelfAssignableRoles() {
            return selfAssignableRoles;
        }

        public GuildTypePlaylist getPlaylist() {
            return playlist;
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

package com.avairebot.orion.database.transformers;

import com.avairebot.orion.contracts.database.transformers.Transformer;
import com.avairebot.orion.database.collection.DataRow;
import com.google.gson.Gson;

public class GuildTypeTransformer extends Transformer {

    private static final Gson GSON = new Gson();

    private String name = "Default";
    private GuildTypeLimits limits = new GuildTypeLimits();

    GuildTypeTransformer(DataRow data) {
        super(data);

        if (hasData()) {
            if (data.getString("type_name", null) != null) {
                name = data.getString("type_name");
            }

            if (data.getString("type_limits", null) != null) {
                GuildTypeLimits typeLimits = GSON.fromJson(data.getString("type_limits"), GuildTypeLimits.class);
                if (typeLimits != null) {
                    limits = typeLimits;
                }
            }
        }
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

        public int getAliases() {
            return aliases;
        }

        public GuildTypePlaylist getPlaylist() {
            return playlist;
        }

        public class GuildTypePlaylist {
            private int lists;
            private int songs;

            public int getPlaylists() {
                return lists;
            }

            public int getSongs() {
                return songs;
            }
        }
    }
}

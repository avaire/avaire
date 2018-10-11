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

import com.avairebot.commands.Category;
import com.avairebot.contracts.database.transformers.Transformer;
import com.avairebot.contracts.debug.Evalable;
import com.avairebot.database.collection.DataRow;
import com.google.gson.internal.LinkedTreeMap;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class ChannelTransformer extends Transformer {

    private final GuildTransformer guildTransformer;
    private final BooleanModule ai = new BooleanModule();
    private final MessageModule welcome = new MessageModule();
    private final MessageModule goodbye = new MessageModule();

    public ChannelTransformer(DataRow data) {
        this(data, null);
    }

    public ChannelTransformer(DataRow data, GuildTransformer guildTransformer) {
        super(data);

        this.guildTransformer = guildTransformer;

        if (hasData()) {
            if (data.get("ai", null) != null) {
                DataRow aiData = new DataRow((LinkedTreeMap<String, Object>) data.get("ai"));

                ai.setEnabled(aiData.getBoolean("enabled", true));
            }

            if (data.get("welcome", null) != null) {
                DataRow welcomeData = new DataRow((LinkedTreeMap<String, Object>) data.get("welcome"));

                welcome.setEnabled(welcomeData.getBoolean("enabled", false));
                welcome.setMessage(welcomeData.getString("message", null));
                welcome.setEmbedColor(welcomeData.getString("embed", null));
            }

            if (data.get("goodbye", null) != null) {
                DataRow goodbyeData = new DataRow((LinkedTreeMap<String, Object>) data.get("goodbye"));

                goodbye.setEnabled(goodbyeData.getBoolean("enabled", false));
                goodbye.setMessage(goodbyeData.getString("message", null));
                goodbye.setEmbedColor(goodbyeData.getString("embed", null));
            }
        }
    }

    public ChannelTransformer(HashMap<String, Object> data) {
        this(new DataRow(data));
    }

    public String getId() {
        return data.getString("id", "invalid-id");
    }

    public BooleanModule getAI() {
        return ai;
    }

    public MessageModule getWelcome() {
        return welcome;
    }

    public MessageModule getGoodbye() {
        return goodbye;
    }

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> objects = new HashMap<>();

        objects.put("ai", ai.toMap());
        objects.put("welcome", welcome.toMap());
        objects.put("goodbye", goodbye.toMap());

        return objects;
    }

    public boolean isCategoryEnabled(@Nonnull Category category) {
        return isCategoryEnabled(category.getName());
    }

    public boolean isCategoryEnabled(@Nonnull String category) {
        if (!isCategoryEnabledGlobally(category)) {
            return false;
        }

        if (guildTransformer.getCategories().containsKey(getId())) {
            return guildTransformer.getCategories().get(getId())
                .getOrDefault(category.toLowerCase(), "true")
                .equalsIgnoreCase("true");
        }

        return true;
    }

    public boolean isCategoryEnabledGlobally(@Nonnull Category category) {
        return isCategoryEnabledGlobally(category.getName());
    }

    public boolean isCategoryEnabledGlobally(@Nonnull String category) {
        if (guildTransformer.getCategories().containsKey("all")) {
            return guildTransformer.getCategories().get("all")
                .getOrDefault(category.toLowerCase(), "true")
                .equalsIgnoreCase("true");
        }
        return true;
    }

    public class MessageModule extends Evalable {
        private boolean enabled = false;
        private String message = null;
        private String embedColor = null;

        public String getEmbedColor() {
            return embedColor;
        }

        public void setEmbedColor(String embedColor) {
            this.embedColor = embedColor;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        HashMap<String, Object> toMap() {
            HashMap<String, Object> objects = new HashMap<>();

            objects.put("enabled", enabled);
            objects.put("message", message);
            objects.put("embed", embedColor);

            return objects;
        }
    }

    public class BooleanModule extends Evalable {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        HashMap<String, Object> toMap() {
            HashMap<String, Object> objects = new HashMap<>();

            objects.put("enabled", enabled);

            return objects;
        }
    }

    public class SlowmodeModule extends Evalable {
        private int limit = 1;
        private int decay = 5;
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getDecay() {
            return decay;
        }

        public void setDecay(int decay) {
            this.decay = decay;
        }

        HashMap<String, Object> toMap() {
            HashMap<String, Object> objects = new HashMap<>();

            objects.put("enabled", enabled);
            objects.put("messagesPerLimit", limit);
            objects.put("messageLimit", decay);

            return objects;
        }
    }
}

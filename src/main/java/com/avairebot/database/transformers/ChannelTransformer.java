package com.avairebot.database.transformers;

import com.avairebot.commands.Category;
import com.avairebot.contracts.database.transformers.Transformer;
import com.avairebot.database.collection.DataRow;
import com.google.gson.internal.LinkedTreeMap;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class ChannelTransformer extends Transformer {

    private final GuildTransformer guildTransformer;
    private final BooleanModule ai = new BooleanModule();
    private final MessageModule welcome = new MessageModule();
    private final MessageModule goodbye = new MessageModule();
    private final SlowmodeModule slowmode = new SlowmodeModule();

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

            if (data.get("slowmode", null) != null) {
                DataRow slowmodeData = new DataRow((LinkedTreeMap<String, Object>) data.get("slowmode"));

                if (slowmodeData.getBoolean("enabled", false)) {
                    slowmode.setEnabled(true);
                    slowmode.setDecay(slowmodeData.getInt("messageLimit", 5));
                    slowmode.setLimit(slowmodeData.getInt("messagesPerLimit", 1));
                }
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

    public SlowmodeModule getSlowmode() {
        return slowmode;
    }

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> objects = new HashMap<>();

        objects.put("ai", ai.toMap());
        objects.put("welcome", welcome.toMap());
        objects.put("goodbye", goodbye.toMap());
        objects.put("slowmode", slowmode.toMap());

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

    public class MessageModule {
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

    public class BooleanModule {
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

    public class SlowmodeModule {
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

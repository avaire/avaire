package com.avairebot.language;

import com.avairebot.AvaIre;
import com.avairebot.config.YamlConfiguration;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Locale;

public class I18n {

    public static final LanguageLocale DEFAULT = new LanguageLocale(new Locale("en", "US"), "en_US", "English");
    public static final HashMap<String, LanguageLocale> LANGS = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(I18n.class);

    private static AvaIre avaire;

    public static void start(AvaIre avaire) {
        I18n.avaire = avaire;

        LANGS.put("en_US", DEFAULT);
        LANGS.put("en_PT", new LanguageLocale(new Locale("en", "US"), "en_PT", "Pirate Speak"));
        LANGS.put("de_DE", new LanguageLocale(new Locale("de", "DE"), "de_DE", "Deutsch"));

        LOGGER.info("Loaded " + LANGS.size() + " languages: " + LANGS);
    }

    @Nullable
    public static String getString(@Nullable Guild guild, String string) {
        if (string == null) {
            return null;
        }
        return get(guild).getString(string, DEFAULT.getConfig().getString(string, null));
    }

    @Nonnull
    public static YamlConfiguration get(@Nullable Guild guild) {
        if (guild == null) {
            return DEFAULT.getConfig();
        }
        return getLocale(guild).getConfig();
    }

    @Nonnull
    public static LanguageLocale getLocale(@Nonnull Guild guild) {
        try {
            GuildTransformer transformer = GuildController.fetchGuild(avaire, guild);

            return LANGS.getOrDefault(transformer == null ? "" : transformer.getLocale(), DEFAULT);
        } catch (Exception e) {
            LOGGER.error("Error when reading entity", e);
            return DEFAULT;
        }
    }
}

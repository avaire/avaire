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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class I18n {

    public static final LanguageHolder DEFAULT = new LanguageHolder(Language.EN_US);
    public static final Set<LanguageHolder> LANGS = new HashSet<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(I18n.class);

    private static AvaIre avaire;

    public static void start(AvaIre avaire) {
        I18n.avaire = avaire;

        LANGS.add(DEFAULT);
        for (Language language : Language.values()) {
            if (DEFAULT.getLanguage().equals(language)) {
                continue;
            }
            LANGS.add(new LanguageHolder(language));
        }

        LOGGER.info("Loaded " + LANGS.size() + " languages: " + LANGS);
    }

    public static String getString(@Nonnull Guild guild, String string, Object... args) {
        String message = getString(guild, string);
        if (message == null) {
            return null;
        }
        return format(message, args);
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
    public static LanguageHolder getLocale(@Nonnull Guild guild) {
        try {
            GuildTransformer transformer = GuildController.fetchGuild(avaire, guild);

            if (transformer != null) {
                return getLocale(transformer);
            }
            return DEFAULT;
        } catch (Exception e) {
            LOGGER.error("Error when reading entity", e);
        }
        return DEFAULT;
    }

    @Nonnull
    public static LanguageHolder getLocale(@Nonnull GuildTransformer transformer) {
        try {
            for (LanguageHolder locale : LANGS) {
                if (locale.getLanguage().getCode().equalsIgnoreCase(transformer.getLocale())) {
                    return locale;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error when reading entity", e);
        }
        return DEFAULT;
    }

    @Nonnull
    public static LanguageHolder getLocale(Language language) {
        for (LanguageHolder locale : LANGS) {
            if (locale.getLanguage().equals(language)) {
                return locale;
            }
        }
        return DEFAULT;
    }

    public static String format(@Nonnull String message, Object... args) {
        int argNum = 0;
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }

            try {
                message = message.replaceFirst(
                    Pattern.quote("{" + (argNum++) + "}"),
                    arg.toString()
                );
            } catch (Exception ex) {
                LOGGER.error(
                    "An exception was through while formatting \"{}\", error: {}",
                    message, ex.getMessage(), ex
                );
            }
        }
        return message;
    }
}

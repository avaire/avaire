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
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

public class I18n {

    static final Set<LanguageHolder> languages = new HashSet<>();
    private static final LanguageHolder defaultLanguage = new LanguageHolder(Language.EN_US);
    private static final Logger log = LoggerFactory.getLogger(I18n.class);

    private static AvaIre avaire;

    public static void start(AvaIre avaire) {
        I18n.avaire = avaire;

        languages.add(defaultLanguage);
        for (Language language : Language.values()) {
            if (defaultLanguage.getLanguage().equals(language)) {
                continue;
            }
            languages.add(new LanguageHolder(language));
        }

        log.info("Loaded " + languages.size() + " languages: " + languages);
    }

    public static LanguageHolder getDefaultLanguage() {
        return defaultLanguage;
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
        return get(guild).getString(string, defaultLanguage.getConfig().getString(string, null));
    }

    @Nonnull
    public static YamlConfiguration get(@Nullable Guild guild) {
        if (guild == null) {
            return defaultLanguage.getConfig();
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
            return defaultLanguage;
        } catch (Exception e) {
            log.error("Error when reading entity", e);
        }
        return defaultLanguage;
    }

    @Nonnull
    public static LanguageHolder getLocale(@Nonnull GuildTransformer transformer) {
        try {
            for (LanguageHolder locale : languages) {
                if (locale.getLanguage().getCode().equalsIgnoreCase(transformer.getLocale())) {
                    return locale;
                }
            }
        } catch (Exception e) {
            log.error("Error when reading entity", e);
        }
        return defaultLanguage;
    }

    @Nonnull
    public static LanguageHolder getLocale(Language language) {
        for (LanguageHolder locale : languages) {
            if (locale.getLanguage().equals(language)) {
                return locale;
            }
        }
        return defaultLanguage;
    }

    public static String format(@Nonnull String message, Object... args) {
        int num = 0;
        Object[] arguments = new Object[args.length];
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            arguments[num++] = arg.toString();
        }

        try {
            return MessageFormat.format(
                message.replace("'", "''"), arguments
            );
        } catch (IllegalArgumentException ex) {
            log.error(
                "An exception was through while formatting \"{}\", error: {}",
                message, ex.getMessage(), ex
            );
            return message;
        }
    }
}

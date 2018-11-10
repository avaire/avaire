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

    static final Set<LanguageContainer> languages = new HashSet<>();
    private static final LanguageContainer defaultLanguage = new LanguageContainer(Language.EN_US);
    private static final Logger log = LoggerFactory.getLogger(I18n.class);

    private static AvaIre avaire;

    /**
     * Starts the internationalization and localization container,
     * loading all the languages into memory, this method should
     * only be called once during the startup of the bot.
     *
     * @param avaire The main AvaIre application instance.
     */
    public static void start(AvaIre avaire) {
        I18n.avaire = avaire;

        languages.add(defaultLanguage);
        for (Language language : Language.values()) {
            if (defaultLanguage.getLanguage().equals(language)) {
                continue;
            }
            languages.add(new LanguageContainer(language));
        }

        log.info("Loaded " + languages.size() + " languages: " + languages);
    }

    /**
     * Gets the default language container, the language and
     * all the strings attached to the language can be
     * loaded through the language container.
     *
     * @return The default language container.
     */
    public static LanguageContainer getDefaultLanguage() {
        return defaultLanguage;
    }

    /**
     * Gets the given string from the guilds selected language, formatting
     * the string with the given arguments, if no string was found for
     * the guilds selected language, the default language will be
     * used instead, if no matches was found there either,
     * then <code>NULL</code> will be returned instead.
     *
     * @param guild  The JDA guild instance that should be used for loading the language.
     * @param string The string that should be loaded from the language files.
     * @param args   The arguments that should be formatted for the given language string.
     * @return The language string from the given guilds selected language, or the default
     * language if it doesn't exists in the guilds selected language, or
     * <code>NULL</code> if it doesn't exist anywhere.
     */
    @Nullable
    public static String getString(@Nonnull Guild guild, String string, Object... args) {
        String message = getString(guild, string);
        if (message == null) {
            return null;
        }
        return format(message, args);
    }

    /**
     * Gets the given string from the guilds selected language, if not string
     * was found for the guilds selected language, the default language
     * will be used instead, if no matches was found there either,
     * then <code>NULL</code> will be returned instead.
     *
     * @param guild  The JDA guild instance that should be used for loading the language.
     * @param string The string that should be loaded from the language files.
     * @return The language string from the given guilds selected language, or the default
     * language if it doesn't exists in the guilds selected language, or
     * <code>NULL</code> if it doesn't exist anywhere.
     */
    @Nullable
    public static String getString(@Nullable Guild guild, String string) {
        if (string == null) {
            return null;
        }
        return get(guild).getString(string, defaultLanguage.getConfig().getString(string, null));
    }

    /**
     * Gets the {@link YamlConfiguration language configuration} for the given guild, the
     * {@link YamlConfiguration language configuration} can be used to load strings
     * directly from the language file, if the guild doesn't have a language
     * selected, or the language that is selected is invalid, the
     * default language will be returned instead.
     *
     * @param guild The JDA guild instance that the language should be loaded for.
     * @return The language the guild instance uses, or the default language if <code>NULL</code>
     * is given or an invalid language is selected by the guild.
     */
    @Nonnull
    public static YamlConfiguration get(@Nullable Guild guild) {
        if (guild == null) {
            return defaultLanguage.getConfig();
        }
        return getLocale(guild).getConfig();
    }

    /**
     * Gets the {@link LanguageContainer language container} for the given guild,
     * the {@link LanguageContainer language container} can be used to get the
     * language strings as well as what type of language it is, if the guild
     * doesn't have a language selected, or the language that is selected
     * is invalid, the default language will be returned instead.
     *
     * @param guild The JDA guild instance that the language container should be loaded for.
     * @return The language container the guild instance uses, or the default language
     * container if the given guild doesn't have a valid language selected.
     */
    @Nonnull
    public static LanguageContainer getLocale(@Nonnull Guild guild) {
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

    /**
     * Gets the {@link LanguageContainer language container} for the given guild
     * transformer, the {@link LanguageContainer language container} can be
     * used to get the language strings as well as what type of language
     * it is, if the guild transformer doesn't have a language
     * selected, or the language that is selected is invalid,
     * the default language will be returned instead.
     *
     * @param transformer The guild transformer that the language container should be loaded from.
     * @return The language container the guild instance uses, or the default language
     * container if the given guild doesn't have a valid language selected.
     */
    @Nonnull
    public static LanguageContainer getLocale(@Nonnull GuildTransformer transformer) {
        try {
            for (LanguageContainer locale : languages) {
                if (locale.getLanguage().getCode().equalsIgnoreCase(transformer.getLocale())) {
                    return locale;
                }
            }
        } catch (Exception e) {
            log.error("Error when reading entity", e);
        }
        return defaultLanguage;
    }

    /**
     * Gets the {@link LanguageContainer language container} matching the given language
     * type, if an invalid type is given the default language will be returned instead.
     *
     * @param language The language type that should be loaded.
     * @return The language container for the given language, or the default language
     * container if the given language is not registered.
     */
    @Nonnull
    public static LanguageContainer getLocale(Language language) {
        for (LanguageContainer locale : languages) {
            if (locale.getLanguage().equals(language)) {
                return locale;
            }
        }
        return defaultLanguage;
    }

    /**
     * Formats the given string with the given arguments using the language formatting,
     * each argument given will be replaced with <code>{index number}</code>, so the
     * first argument will be replaced with any instances of <code>{0}</code>, the
     * second will be replaced with any instances of <code>{1}</code>, etc.
     * <p>
     * Every argument given can be replaced multiple times per string, the placement of the
     * placeholders(<code>{0}</code>, <code>{1}</code>, etc) doesn't matter either, giving
     * developers free rein to format and structure their messages however they want to.
     *
     * @param message The message that should be formatted with the given arguments.
     * @param args    The arguments that should be replaced in the given message.
     * @return The formatted string, or the original string if the formatting process
     * failed due to an invalid argument exception.
     */
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

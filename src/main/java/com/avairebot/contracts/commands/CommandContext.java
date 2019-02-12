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

package com.avairebot.contracts.commands;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandContainer;
import com.avairebot.config.YamlConfiguration;
import com.avairebot.database.controllers.PlayerController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlayerTransformer;
import com.avairebot.handlers.DatabaseEventHolder;
import net.dv8tion.jda.core.entities.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface CommandContext {

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.Guild Guild} that this message was sent in.
     * <br>This is just a shortcut to {@link net.dv8tion.jda.core.entities.TextChannel#getGuild() getChannel().getGuild()}.
     * <br><b>This is only valid if the Message was actually sent in a TextChannel.</b> This will return {@code null}
     * if it was not sent from a TextChannel.
     * <br>You can check the type of channel this message was sent from using {@link #getMessage() getMessage().getChannelType()}.
     *
     * @return The Guild this message was sent in, or {@code null} if it was not sent from a TextChannel.
     * @throws java.lang.UnsupportedOperationException If this is not a Received Message from {@link net.dv8tion.jda.core.entities.MessageType#DEFAULT MessageType.DEFAULT}
     */
    Guild getGuild();

    /**
     * Returns the author of this Message as a {@link net.dv8tion.jda.core.entities.Member member}.
     * <br>This is just a shortcut to {@link #getGuild()}{@link net.dv8tion.jda.core.entities.Guild#getMember(User) .getMember(getAuthor())}.
     * <br><b>This is only valid if the Message was actually sent in a TextChannel.</b> This will return {@code null}
     * if it was not sent from a TextChannel.
     * <br>You can check the type of channel this message was sent from using {@link #getMessage() getMessage().getChannelType()}.
     *
     * @return Message author, or {@code null} if the message was not sent from a TextChannel.
     * @throws java.lang.UnsupportedOperationException If this is not a Received Message from {@link net.dv8tion.jda.core.entities.MessageType#DEFAULT MessageType.DEFAULT}
     */
    Member getMember();

    /**
     * The author of this Message.
     *
     * @return Message author
     * @throws java.lang.UnsupportedOperationException If this is not a Received Message from {@link net.dv8tion.jda.core.entities.MessageType#DEFAULT MessageType.DEFAULT}
     */
    User getAuthor();

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that this message was sent in.
     * <br><b>This is only valid if the Message was actually sent in a TextChannel.</b> This will return {@code null}
     * if it was not sent from a TextChannel.
     * <br>You can check the type of channel this message was sent from using {@link #getMessage() getMessage().getChannelType()}.
     * <br>
     * <p>Use {@link #getMessageChannel()} for an ambiguous {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     * if you do not need functionality specific to {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     *
     * @return The TextChannel this message was sent in, or {@code null} if it was not sent from a TextChannel.
     * @throws java.lang.UnsupportedOperationException If this is not a Received Message from {@link net.dv8tion.jda.core.entities.MessageType#DEFAULT MessageType.DEFAULT}
     */
    TextChannel getChannel();

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} that this message was sent in.
     *
     * @return The MessageChannel of this Message
     * @throws java.lang.UnsupportedOperationException If this is not a Received Message from {@link net.dv8tion.jda.core.entities.MessageType#DEFAULT MessageType.DEFAULT}
     */
    MessageChannel getMessageChannel();

    /**
     * The received {@link net.dv8tion.jda.core.entities.Message Message} object.
     *
     * @return The received {@link net.dv8tion.jda.core.entities.Message Message} object.
     */
    Message getMessage();

    /**
     * Returns the {@link GuildTransformer guild transformer} instance linked to the current message,
     * if the message was sent as a direct message, or an error occurred while loading the
     * {@link GuildTransformer transformer} from the database this will return null.
     * <p>
     * This is just a shortcut for calling {@link #getDatabaseEventHolder()}{@link DatabaseEventHolder#getGuild()}.
     *
     * @return The {@link GuildTransformer guild transformer} instance linked to the current guild.
     */
    @Nullable
    GuildTransformer getGuildTransformer();

    /**
     * Returns the {@link PlayerTransformer player transformer} instance linked to the current message, if
     * the message was sent as a direct message, or the server the message was sent in has leveling
     * disabled, or an error occurred while loading the {@link PlayerTransformer transformer}
     * from the database this will return null.
     * <p>
     * This is just a shortcut for calling {@link #getDatabaseEventHolder()}{@link DatabaseEventHolder#getPlayer()}.
     *
     * @return The {@link PlayerTransformer player transformer} instance linked to the current guild.
     */
    @Nullable
    PlayerTransformer getPlayerTransformer();

    /**
     * Returns the {@link PlayerTransformer player transformer} instance linked to the current message, if
     * the {@link PlayerTransformer player transformer} wasn't autoloaded because the server has leveling
     * disabled, the method will attempt to load the player transformer directly from
     * the {@link PlayerController player controller} instead.
     * <p>
     * This is just a shortcut for calling {@link #getDatabaseEventHolder()}{@link DatabaseEventHolder#getPlayer()},
     * but if that returns null, then {@link PlayerController#fetchPlayer(AvaIre, Message)}
     * will automatically be called instead.
     *
     * @param avaire The main AvaIre instance used to communicate with the rest of the application.
     * @return Possibly-null, the {@link PlayerTransformer player transformer} for the author of
     * command context for the current guild, or {@code NULL} if something went wrong.
     */
    @Nullable
    default PlayerTransformer getPlayerTransformerWithForce(@Nonnull AvaIre avaire) {
        PlayerTransformer playerTransformer = getPlayerTransformer();
        if (playerTransformer != null) {
            return playerTransformer;
        }
        return PlayerController.fetchPlayer(avaire, getMessage());
    }

    /**
     * Returns the database event holder, when a message is received by Ava, the users {@link PlayerTransformer player inforatmion},
     * as well as the servers {@link GuildTransformer guild info} is loaded from the database and stored in memory, this object
     * is used for holding both objects to easily fetch guild settings, changing settings, getting player XP, etc.
     * <p>
     * For shortcuts when getting information from the database event holder, you can use
     * the {@link #getGuildTransformer()} and {@link #getPlayerTransformer()} methods.
     *
     * @return The database event holder.
     */
    @Nullable
    DatabaseEventHolder getDatabaseEventHolder();

    /**
     * An immutable list of all mentioned {@link net.dv8tion.jda.core.entities.User Users}.
     * <br>If no user was mentioned, this list is empty, if the bot was mentioned as part of
     * invoking the command, the {@link #isMentionableCommand() isMentionableCommand()}
     * should be true, and the bot will be stripped from the mentioned users list.
     *
     * @return immutable list of mentioned users
     * @throws java.lang.UnsupportedOperationException If this is not a Received Message from {@link net.dv8tion.jda.core.entities.MessageType#DEFAULT MessageType.DEFAULT}
     */
    List<User> getMentionedUsers();

    /**
     * A immutable list of all mentioned {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}.
     * <br>If none were mentioned, this list is empty.
     * <br>
     * <p><b>This may include TextChannels from other {@link net.dv8tion.jda.core.entities.Guild Guilds}</b>
     *
     * @return immutable list of mentioned TextChannels
     * @throws java.lang.UnsupportedOperationException If this is not a Received Message from {@link net.dv8tion.jda.core.entities.MessageType#DEFAULT MessageType.DEFAULT}
     */
    List<TextChannel> getMentionedChannels();

    /**
     * Returns true if the message was invoked as a mentionable command, this means
     * that the command was invoked by mentioning the bot instead of using the
     * defined prefix for the command category.
     *
     * @return True if the command was invoked through a mention.
     */
    boolean isMentionableCommand();

    /**
     * Indicates if this Message mentions everyone using @everyone or @here.
     *
     * @return True, if message is mentioning everyone.
     */
    boolean mentionsEveryone();

    /**
     * Returns true if the message was sent in a guild.
     *
     * @return True if the message was sent in a guild.
     */
    boolean isGuildMessage();

    /**
     * Whether we can send messages in the channel the command was invoked in.
     * <p>
     * This is an overload of {@link TextChannel#canTalk(Member)} with the SelfMember, if
     * the command was invoked in a DM, it will always return true.
     * <p>
     * Checks for both {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ},
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}, and
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_EMBED_LINKS Permission#MESSAGE_EMBED_LINKS}.
     *
     * @return True, if we are able to read and send messages in this channel.
     */
    boolean canTalk();

    /**
     * Returns the {@link YamlConfiguration configuration} for the current selected {@link com.avairebot.language.I18n I18n}
     * language, if the message was sent as a direct message, or if the guild doesn't have a valid language
     * selected the {@link com.avairebot.language.I18n#defaultLanguage default language} will be used instead.
     *
     * @return The selected {@link YamlConfiguration configuration} for the current messages {@link com.avairebot.language.I18n I18n} language.
     */
    @Nonnull
    YamlConfiguration getI18n();

    /**
     * Gets a string from the {@link com.avairebot.language.I18n I18n} language file with the
     * command as a prefix, if you'd like to get a message from the language file without
     * using the command prefix you can use the {@link #i18nRaw(String)} method.
     * <br>
     * <p>The command prefix is the command category name in all lowercase, followed by a dot,
     * followed by the commands class name, any commands specific messages can be put at
     * this path for Ava to easily find then.
     * <br>
     * <p><b>For example if we were to use the method from the Ping command:</b>
     * <pre><code>
     * context.i18n("rating.10"); // Gets the i18n string: utility.PingCommand.rating.10
     * </code></pre>
     * Which would look like this in YAML
     * <pre><code>
     * utility:
     *   PingCommand:
     *     rating:
     *       10: 'faster than Sonic! :smiley_cat:'
     * </code></pre>
     *
     * @param key The key of the {@link com.avairebot.language.I18n I18n} message.
     * @return Possibly-null, the message that matches the given I18n string, or null if it doesn't exists.
     */
    @CheckReturnValue
    String i18n(@Nonnull String key);

    /**
     * Gets a string from the {@link com.avairebot.language.I18n I18n} language file with the
     * command as a prefix, if you'd like to get a message from the language file without
     * using the command prefix you can use the {@link #i18nRaw(String)} method.
     * <br>
     * <p>The command prefix is the command category name in all lowercase, followed by a dot,
     * followed by the commands class name, any commands specific messages can be put at
     * this path for Ava to easily find then.
     * <br>
     * <p><b>For example if we were to use the method from the uptime command:</b>
     * <pre><code>
     * context.i18n("footer", "First", "Second");
     * // Gets the i18n string: utility.UptimeCommand.footer
     * // And gives the language string two replaceable arguments
     * </code></pre>
     * Which would look like this in YAML
     * <pre><code>
     * utility:
     *   UptimeCommand:
     *     footer: 'Started {0} at {1}'
     * </code></pre>
     * <p>
     * Which produces the following output
     * <pre><code>
     * Started First at Second
     * </code></pre>
     *
     * @param key  The key of the {@link com.avairebot.language.I18n I18n} message.
     * @param args The arguments that should be replaced in the language message.
     * @return Possibly-null, the message that matches the given I18n string, or null if it doesn't exists.
     */
    @CheckReturnValue
    String i18n(@Nonnull String key, Object... args);

    /**
     * Gets a raw string from the {@link com.avairebot.language.I18n I18n} language file, this will ignore the command
     * prefix, if you'd like to get a message using the command prefix you can use the {@link #i18n(String)} method.
     * <br>
     * <p><b>For example if we were to use the method from the Ping command:</b>
     * <pre><code>
     * context.i18n("utility.PingCommand.rating.10"); // Gets the i18n string: utility.PingCommand.rating.10
     * </code></pre>
     * Which would look like this in YAML
     * <pre><code>
     * utility:
     *   PingCommand:
     *     rating:
     *       10: 'faster than Sonic! :smiley_cat:'
     * </code></pre>
     *
     * @param key The key of the {@link com.avairebot.language.I18n I18n} message.
     * @return Possibly-null, the message that matches the given I18n string, or null if it doesn't exists.
     */
    @CheckReturnValue
    String i18nRaw(@Nonnull String key);

    /**
     * Gets a raw string from the {@link com.avairebot.language.I18n I18n} language file, this will ignore the command
     * prefix, if you'd like to get a message using the command prefix you can use the {@link #i18n(String)} method.
     * <br>
     * <p><b>For example if we were to use the method from the uptime command:</b>
     * <pre><code>
     * context.i18nRaw("utility.UptimeCommand.footer", "First", "Second");
     * // Gets the i18n string: utility.UptimeCommand.footer
     * // And gives the language string two replaceable arguments
     * </code></pre>
     * Which would look like this in YAML
     * <pre><code>
     * utility:
     *   UptimeCommand:
     *     footer: 'Started {0} at {1}'
     * </code></pre>
     * <p>
     * Which produces the following output
     * <pre><code>
     * Started First at Second
     * </code></pre>
     *
     * @param key  The key of the {@link com.avairebot.language.I18n I18n} message.
     * @param args The arguments that should be replaced in the language message.
     * @return Possibly-null, the message that matches the given I18n string, or null if it doesn't exists.
     */
    @CheckReturnValue
    String i18nRaw(@Nonnull String key, Object... args);

    /**
     * Gets the {@link com.avairebot.language.I18n I18n} command prefix.
     *
     * @return The {@link com.avairebot.language.I18n I18n} command prefix.
     */
    String getI18nCommandPrefix();

    /**
     * Generates the new {@link com.avairebot.language.I18n I18n} command prefix off the {@link CommandContainer Command Container}, then calls {@link #setI18nPrefix(String)} with the generated message, command prefixes are generated using the following format:
     * <br>
     * <pre><code>
     * command category.command class name.
     * </code></pre>
     * <br>
     * So the Ping command prefix would look like this:
     * <br>
     * <pre><code>
     * utility.PingCommand.
     * </code></pre>
     *
     * @param container The {@link CommandContainer Command Container} to use for generating the {@link com.avairebot.language.I18n I18n} prefix.
     */
    void setI18nCommandPrefix(@Nonnull CommandContainer container);

    /**
     * Sts the {@link com.avairebot.language.I18n I18n} command prefix to the given string.
     *
     * @param i18nPrefix The string the {@link com.avairebot.language.I18n I18n} prefix should be set to.
     */
    void setI18nPrefix(@Nullable String i18nPrefix);
}

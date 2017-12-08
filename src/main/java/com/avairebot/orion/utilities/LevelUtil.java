package com.avairebot.orion.utilities;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.chat.MessageType;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.controllers.PlayerController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.database.transformers.PlayerTransformer;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.sql.SQLException;

public class LevelUtil {

    /**
     * The quadratic equation `a` value.
     */
    private static final int A = 5;

    /**
     * The quadratic equation `b` value.
     */
    private static final int B = 50;

    /**
     * The quadratic equation `c` value.
     */
    private static final int C = 100;

    /**
     * Get the amount of experience needed to reach the given level.
     *
     * @param level The level the experience should be fetched from.
     * @return The minimum amount of experience needed to reach the given level.
     */
    public static long getExperienceFromLevel(long level) {
        return (long) (A * Math.pow(level, 2)) + (B * level) + C;
    }

    /**
     * Gets the max level that can be reached with the given amount experience.
     *
     * @param xp The experience that should be resolved into the level.
     * @return The max level that can be reached with the given amount of experience.
     */
    public static long getLevelFromExperience(long xp) {
        if (Math.pow(B, 2) - ((4 * A) * (C - xp)) < 0) {
            throw new RuntimeException("Discriminant is less than zero, no real roots");
        }

        double x = (-B + Math.sqrt(Math.pow(B, 2) - ((4 * A) * (C - xp)))) / (2 * A);
        return x < 0 ? 0 : (long) Math.floor(x);
    }

    /**
     * Reward the player a random amount of experience between 10 and 15 using the
     * {@link MessageReceivedEvent}, the reward is throttled to one use every
     * minute per-guild-player, if the player has received experience in the
     * same guild through this method before in the last minute, nothing
     * will be given to the player/user.
     *
     * @param orion  The Orion application instance.
     * @param event  The event that should be used in rewarding the player.
     * @param guild  The guild transformer from the current guild database instance.
     * @param player The player transformer from the current player database instance.
     */
    public static void rewardPlayer(Orion orion, MessageReceivedEvent event, GuildTransformer guild, PlayerTransformer player) {
        String cacheToken = String.format("user-message-xp-event.%s.%s",
            player.getGuildId(),
            player.getUserId()
        );

        if (orion.getCache().getAdapter(CacheType.MEMORY).has(cacheToken)) {
            return;
        }
        orion.getCache().getAdapter(CacheType.MEMORY).put(cacheToken, 0, 60);

        giveExperience(orion, event.getMessage(), guild, player);
    }

    /**
     * Give the user the given amount of experience, updating the database and
     * saving it to the transformer, storing it temporarily in memory, if the
     * event is not a guild message event the method call will be canceled.
     *
     * @param orion   The Orion application instance.
     * @param message The guild message event that should be used.
     * @param user    The user that should be given the experience.
     * @param amount  The amount of experience that should be given to the user.
     */
    public static void giveExperience(Orion orion, Message message, User user, int amount) {
        if (!message.getChannelType().isGuild()) {
            return;
        }

        giveExperience(orion, message, GuildController.fetchGuild(orion, message), PlayerController.fetchPlayer(orion, message, user), amount);
    }

    /**
     * Give given player a random amount of experience between
     * 10 and 15, updating the database and saving it to the
     * transformer, storing it temporarily in memory.
     *
     * @param orion   The Orion application instance.
     * @param message The guild message event that should be used.
     * @param guild   The guild transformer for the guild the player is from.
     * @param player  The player that should be given the experience.
     */
    public static void giveExperience(Orion orion, Message message, GuildTransformer guild, PlayerTransformer player) {
        giveExperience(orion, message, guild, player, (RandomUtil.getInteger(5) + 10));
    }

    /**
     * Give the user the given amount of experience, updating the database and
     * saving it to the transformer, storing it temporarily in memory.
     *
     * @param orion   The Orion application instance.
     * @param message The guild message event that should be used.
     * @param guild   The guild transformer for the guild the player is from.
     * @param player  The player that should be given the experience.
     * @param amount  The amount of experience that should be given to the player.
     */
    public static void giveExperience(Orion orion, Message message, GuildTransformer guild, PlayerTransformer player, int amount) {
        long exp = player.getExperience();
        long lvl = getLevelFromExperience(exp);

        player.incrementExperienceBy(amount);

        try {
            orion.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                .where("user_id", player.getUserId())
                .andWhere("guild_id", player.getGuildId())
                .update(statement -> statement.set("experience", player.getExperience()));

            if (guild.isLevelAlerts() && getLevelFromExperience(player.getExperience()) > lvl) {
                MessageFactory.makeEmbeddedMessage(getLevelUpChannel(message, guild))
                    .setColor(MessageType.SUCCESS.getColor())
                    .setDescription(String.format("GG <@%s>, you just reached **Level %s**",
                        player.getUserId(),
                        getLevelFromExperience(player.getExperience())
                    )).queue();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets the level up channel from the current message event and guild
     * transformer, if no valid level up channel is set for the given
     * guild, the message text chanel will be returned instead.
     *
     * @param message The message event that should be used as a default.
     * @param guild   The guild transformer that should be used to get the level up channel.
     * @return The level up channel if one is set, otherwise the text channel from the message object.
     */
    private static TextChannel getLevelUpChannel(Message message, GuildTransformer guild) {
        String levelChannel = guild.getLevelChannel();
        if (levelChannel == null) {
            return message.getTextChannel();
        }

        TextChannel channel = message.getGuild().getTextChannelById(levelChannel);
        return channel == null ? message.getTextChannel() : channel;
    }
}

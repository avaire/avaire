package com.avairebot.orion.level;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.database.transformers.PlayerTransformer;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.Random;

public class LevelManager {

    private static final Random RANDOM = new Random();

    private static final int A = 5;
    private static final int B = 50;
    private static final int C = 100;

    public static long getLevelXp(long level) {
        return (long) (A * Math.pow(level, 2)) + (B * level) + C;
    }

    public static long getLevelFromXp(long xp) {
        if (Math.pow(B, 2) - ((4 * A) * (C - xp)) < 0) {
            throw new RuntimeException("Discriminant is less than zero, no real roots");
        }

        double x = (-B + Math.sqrt(Math.pow(B, 2) - ((4 * A) * (C - xp)))) / (2 * A);
        return x < 0 ? 0 : (long) Math.floor(x);
    }

    public static void rewardPlayer(Orion orion, MessageReceivedEvent event, GuildTransformer guild, PlayerTransformer player) {
        String cacheToken = String.format("user-message-xp-event.%s.%s",
                player.getGuildId(),
                player.getUserId()
        );

        if (orion.cache.getAdapter(CacheType.MEMORY).has(cacheToken)) {
            return;
        }
        orion.cache.getAdapter(CacheType.MEMORY).put(cacheToken, 0, 60);

        rewardPlayer(orion, event.getMessage(), guild, player);
    }

    public static void rewardPlayer(Orion orion, Message message, GuildTransformer guild, PlayerTransformer player) {
        rewardPlayer(orion, message, guild, player, (RANDOM.nextInt(5) + 10));
    }

    public static void rewardPlayer(Orion orion, Message message, GuildTransformer guild, PlayerTransformer player, int amount) {
        long exp = player.getExperience();
        long lvl = getLevelFromXp(exp);

        player.incrementExperienceBy(amount);

        try {
            orion.database.newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                    .where("user_id", player.getUserId())
                    .andWhere("guild_id", player.getGuildId())
                    .update(statement -> statement.set("experience", player.getExperience()));

            if (guild.isLevelAlerts() && getLevelFromXp(player.getExperience()) > lvl) {
                getLevelUpChannel(message, guild).sendMessage(MessageFactory.createEmbeddedBuilder()
                        .setColor(MessageFactory.MessageType.SUCCESS.getColor())
                        .setDescription(String.format("GG <@%s>, you just reached **Level %s**",
                                player.getUserId(),
                                getLevelFromXp(player.getExperience())
                        )).build()
                ).queue();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static TextChannel getLevelUpChannel(Message message, GuildTransformer guild) {
        String levelChannel = guild.getLevelChannel();
        if (levelChannel == null) {
            return message.getTextChannel();
        }

        TextChannel channel = message.getGuild().getTextChannelById(levelChannel);
        return channel == null ? message.getTextChannel() : channel;
    }
}

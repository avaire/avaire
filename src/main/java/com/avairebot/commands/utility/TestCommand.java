package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.cache.CacheType;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.administration.LevelCommand;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.controllers.PlayerController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlayerTransformer;
import com.avairebot.imagegen.renders.RankBackgroundRender;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"Duplicates"})
public class TestCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(TestCommand.class);
    private final String cacheToken = "database-user-scores.";

    public TestCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Test Command";
    }

    @Override
    public String getDescription() {
        return "Runs the test command to do a thing.";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("test");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null || !guildTransformer.isLevels()) {
            return sendErrorMessage(
                context,
                "errors.requireLevelFeatureToBeEnabled",
                CommandHandler.getCommand(LevelCommand.class)
                    .getCommand().generateCommandTrigger(context.getMessage())
            );
        }

        PlayerTransformer player = context.getDatabaseEventHolder().getPlayer();
        if (player == null) {
            return sendErrorMessage(context, "Player object was null, exiting");
        }

        loadProperties(context, context.getAuthor()).thenAccept(properties -> {
            String score = properties.getScore().equals("Unranked")
                ? "Unranked"
                : properties.getScore();

            long experience = player.getExperience();
            long level = avaire.getLevelManager().getLevelFromExperience(guildTransformer, experience);
            long current = avaire.getLevelManager().getExperienceFromLevel(guildTransformer, level);

            long nextLevelXp = avaire.getLevelManager().getExperienceFromLevel(guildTransformer, level + 1);
            double percentage = ((double) (experience - current) / (nextLevelXp - current)) * 100;

            log.info("Percentage: " + percentage);

            try {
                RankBackgroundRender rankBackgroundRender = new RankBackgroundRender(context.getAuthor());

                final byte[] image = rankBackgroundRender
                    .setRank(score)
                    .setLevel(NumberUtil.formatNicely(level))
                    .setCurrentXpInLevel(NumberUtil.formatNicely(experience - current))
                    .setTotalXpInLevel(NumberUtil.formatNicely(nextLevelXp - current))
                    .setGlobalExperience(NumberUtil.formatNicely(properties.getTotal()))
                    .setServerExperience(NumberUtil.formatNicely(experience - 100))
                    .setPercentage(percentage)
                    .renderToBytes();

                if (image == null) {
                    context.makeError("Failed to generate the image, something that wasn't suppose to happen, happened.").queue();
                    return;
                }

                MessageBuilder message = new MessageBuilder();
                message.setEmbed(context.makeEmbeddedMessage()
                    .setColor(Color.decode("#5C5F93"))
                    .setImage("attachment://" + context.getAuthor().getId() + "-avatar.png")
                    .buildEmbed()
                );
                context.getChannel().sendFile(image, context.getAuthor().getId() + "-avatar.png", message.build()).queue();
            } catch (IOException e) {
                context.makeError("Failed to run test command: " + e.getMessage()).queue();
                e.printStackTrace();
            }
        });

        return true;
    }

    private CompletableFuture<DatabaseProperties> loadProperties(CommandMessage context, User author) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PlayerTransformer player = context.getAuthor().getIdLong() == author.getIdLong()
                    ? context.getPlayerTransformer() : PlayerController.fetchPlayer(avaire, context.getMessage(), author);

                DataRow data = avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                    .selectRaw("sum(`experience`) - (count(`user_id`) * 100) as `total`")
                    .where("user_id", author.getId())
                    .get().first();

                long total = data == null ? (player == null ? 0 : player.getExperience()) : data.getLong("total");

                return new DatabaseProperties(player, total, getScore(context, author.getId()));
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }


    private String getScore(CommandMessage context, String userId) throws SQLException {
        if (avaire.getCache().getAdapter(CacheType.MEMORY).has(cacheToken + context.getGuild().getId())) {
            Collection users = (Collection) avaire.getCache().getAdapter(CacheType.MEMORY).get(cacheToken + context.getGuild().getId());
            String score = "???"; // Unranked score

            for (int i = 0; i < users.size(); i++) {
                if (Objects.equals(users.get(i).getString("id"), userId)) {
                    score = "" + (i + 1);
                    break;
                }
            }

            return score;
        }

        avaire.getCache().getAdapter(CacheType.MEMORY).put(cacheToken + context.getGuild().getId(),
            avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                .select("user_id as id")
                .orderBy("experience", "desc")
                .where("guild_id", context.getGuild().getId())
                .get(),
            120
        );

        return getScore(context, userId);
    }

    private long getUsersInGuild(Guild guild) {
        return guild.getMembers().stream().filter(member -> !member.getUser().isBot()).count();
    }

    private class DatabaseProperties {

        private final PlayerTransformer player;
        private final long total;
        private final String score;

        DatabaseProperties(PlayerTransformer player, long total, String score) {
            this.player = player;
            this.total = total;
            this.score = score;
        }

        public PlayerTransformer getPlayer() {
            return player;
        }

        long getTotal() {
            return total;
        }

        String getScore() {
            return score;
        }
    }
}

package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.commands.CommandPriority;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.database.collection.Collection;
import com.avairebot.orion.database.collection.DataRow;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.controllers.PlayerController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.database.transformers.PlayerTransformer;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.LevelUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class RankCommand extends Command {

    private final String cacheToken = "database-user-scores.";

    public RankCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Rank Command";
    }

    @Override
    public String getDescription() {
        return "Gets your rank, level, xp for the current server and total xp for all servers that you're on, you can tag a user to see their level stats instead."
            + "\nThis command requires the `Levels & Experience` feature to be enabled for the server!";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Displays your rank, level, xp and other stuff",
            "`:command @Senither` - Displays Senither's rank, level, xp...");
    }

    @Override
    public String getExampleUsage() {
        return "`:command`";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("rank", "level");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,1,5");
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.HIGH;
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildTransformer guildTransformer = GuildController.fetchGuild(orion, message);
        if (guildTransformer == null || !guildTransformer.isLevels()) {
            return sendErrorMessage(message, "This command requires the `Levels & Experience` feature to be enabled for the server, you can ask a server admin if they want to enable it with `.level`");
        }

        User user = message.getAuthor();
        if (!message.getMentionedUsers().isEmpty()) {
            user = message.getMentionedUsers().get(0);
        }
        final User author = user;

        if (author.isBot()) {
            MessageFactory.makeWarning(message, "Bots cannot receive xp and therefore can\'t be ranked, try and tag a user instead.").queue();
            return false;
        }

        loadProperties(message, author).thenAccept(properties -> {
            String score = properties.getScore().equals("Unranked") ?
                "Unranked" : properties.getScore() + " / " + message.getGuild().getMembers().size();

            long experience = properties.getPlayer().getExperience();
            long level = LevelUtil.getLevelFromExperience(experience);
            long current = LevelUtil.getExperienceFromLevel(level);

            long diff = LevelUtil.getExperienceFromLevel(level + 1) - current;
            double percentage = ((double) (experience - current) / diff) * 100;

            String levelBar = "";
            for (int i = 1; i <= 40; i++) {
                levelBar += ((i * 2.5) < percentage) ? "\u2592" : "\u2591";
            }

            EmbedBuilder embed = MessageFactory.createEmbeddedBuilder().setColor(Color.decode("#E91E63"))
                .setAuthor(author.getName(), "https://avairebot.com/leaderboard/" + message.getGuild().getId(), author.getAvatarUrl())
                .setFooter("https://avairebot.com/leaderboard/" + message.getGuild().getId(), null)
                .addField("Rank", score, true)
                .addField("Level", "" + level, true)
                .addField("Experience", (experience - 100 < 0 ? "0" : String.format("%s (Total: %s)",
                    experience - 100, properties.getTotal()
                )), true)
                .addField("Experience needed to next Level", String.format("[%s] %s%s",
                    levelBar, new DecimalFormat("#.##").format(percentage), '%'
                ), false);

            message.getChannel().sendMessage(embed.build()).queue();
        });

        return true;
    }

    private CompletableFuture<DatabaseProperties> loadProperties(Message message, User author) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PlayerTransformer player = PlayerController.fetchPlayer(orion, message, author);
                DataRow data = orion.database.newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                    .selectRaw("sum(`experience`) - (count(`user_id`) * 100) as `total`")
                    .where("user_id", author.getId())
                    .get().first();

                long total = data == null ? player.getExperience() : data.getLong("total");

                return new DatabaseProperties(player, total, getScore(message, author.getId()));
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private String getScore(Message message, String userId) throws SQLException {
        if (orion.cache.getAdapter(CacheType.MEMORY).has(cacheToken + message.getGuild().getId())) {
            Collection users = (Collection) orion.cache.getAdapter(CacheType.MEMORY).get(cacheToken + message.getGuild().getId());
            String score = "Unranked";

            for (int i = 0; i < users.size(); i++) {
                if (Objects.equals(users.get(i).getString("id"), userId)) {
                    score = "" + (i + 1);
                    break;
                }
            }

            return score;
        }

        orion.cache.getAdapter(CacheType.MEMORY).put(cacheToken + message.getGuild().getId(),
            orion.database.newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                .select("user_id as id")
                .orderBy("experience", "desc")
                .where("guild_id", message.getGuild().getId())
                .get(),
            120
        );

        return getScore(message, userId);
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

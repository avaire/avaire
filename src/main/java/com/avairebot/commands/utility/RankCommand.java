package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.cache.CacheType;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.commands.administration.LevelCommand;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.controllers.PlayerController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlayerTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.LevelUtil;
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

    public RankCommand(AvaIre avaire) {
        super(avaire, false);
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
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command`");
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
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer guildTransformer = GuildController.fetchGuild(avaire, context.getMessage());
        if (guildTransformer == null || !guildTransformer.isLevels()) {
            return sendErrorMessage(context,
                "This command requires the `Levels & Experience` feature to be enabled for the server, you can ask a server admin if they want to enable it with `%slevel`",
                CommandHandler.getCommand(LevelCommand.class).getCategory().getPrefix(context.getMessage())
            );
        }

        User user = context.getAuthor();
        if (!context.getMentionedUsers().isEmpty() && !args[0].equals("---skip-mentions")) {
            user = context.getMentionedUsers().get(0);
        }
        final User author = user;

        if (author.isBot()) {
            context.makeWarning("Bots cannot receive xp and therefore can\'t be ranked, try and tag a user instead.").queue();
            return false;
        }

        loadProperties(context, author).thenAccept(properties -> {
            String score = properties.getScore().equals("Unranked") ?
                "Unranked" : properties.getScore() + " / " + context.getGuild().getMembers().size();

            long experience = properties.getPlayer().getExperience();
            long level = LevelUtil.getLevelFromExperience(experience);
            long current = LevelUtil.getExperienceFromLevel(level);

            long diff = LevelUtil.getExperienceFromLevel(level + 1) - current;
            double percentage = ((double) (experience - current) / diff) * 100;

            String levelBar = "";
            for (int i = 1; i <= 40; i++) {
                levelBar += ((i * 2.5) < percentage) ? "\u2592" : "\u2591";
            }

            MessageFactory.makeEmbeddedMessage(context.getChannel(), Color.decode("#E91E63"))
                .setAuthor(author.getName(), "https://avairebot.com/leaderboard/" + context.getGuild().getId(), author.getAvatarUrl())
                .setFooter("https://avairebot.com/leaderboard/" + context.getGuild().getId())
                .addField("Rank", score, true)
                .addField("Level", "" + level, true)
                .addField("Experience", (experience - 100 < 0 ? "0" : String.format("%s (Total: %s)",
                    experience - 100, properties.getTotal()
                )), true)
                .addField("Experience needed to next Level", String.format("[%s] %s%s",
                    levelBar, new DecimalFormat("#.##").format(percentage), '%'
                ), false)
                .queue();
        });

        return true;
    }

    private CompletableFuture<DatabaseProperties> loadProperties(CommandMessage message, User author) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PlayerTransformer player = PlayerController.fetchPlayer(avaire, message.getMessage(), author);
                DataRow data = avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                    .selectRaw("sum(`experience`) - (count(`user_id`) * 100) as `total`")
                    .where("user_id", author.getId())
                    .get().first();

                long total = data == null ? player.getExperience() : data.getLong("total");

                return new DatabaseProperties(player, total, getScore(message.getMessage(), author.getId()));
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private String getScore(Message message, String userId) throws SQLException {
        if (avaire.getCache().getAdapter(CacheType.MEMORY).has(cacheToken + message.getGuild().getId())) {
            Collection users = (Collection) avaire.getCache().getAdapter(CacheType.MEMORY).get(cacheToken + message.getGuild().getId());
            String score = "Unranked";

            for (int i = 0; i < users.size(); i++) {
                if (Objects.equals(users.get(i).getString("id"), userId)) {
                    score = "" + (i + 1);
                    break;
                }
            }

            return score;
        }

        avaire.getCache().getAdapter(CacheType.MEMORY).put(cacheToken + message.getGuild().getId(),
            avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
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

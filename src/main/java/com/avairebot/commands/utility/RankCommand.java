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

package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.commands.administration.LevelCommand;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.controllers.PlayerController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlayerTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.imagegen.RankBackgrounds;
import com.avairebot.imagegen.renders.RankBackgroundRender;
import com.avairebot.language.I18n;
import com.avairebot.utilities.CacheUtil;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.NumberUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RankCommand extends Command {

    public static final Cache<Long, Collection> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(120, TimeUnit.SECONDS)
        .build();

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
        return Collections.singletonList("`:command @Senither`");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            LeaderboardCommand.class,
            GlobalLeaderboardCommand.class,
            RankBackgroundCommand.class
        );
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

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.LEVEL_AND_EXPERIENCE);
    }

    @Override
    @SuppressWarnings({"SingleStatementInBlock", "ConstantConditions"})
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

        User user = context.getAuthor();
        if (args.length > 0 && !args[0].equals("---skip-mentions")) {
            user = MentionableUtil.getUser(context, new String[]{String.join(" ", args)});
            if (user == null) {
                user = context.getAuthor();
            }
        }
        final User author = user;

        if (author.isBot()) {
            context.makeWarning(context.i18n("botsCannotReceiveXp")).queue();
            return false;
        }

        loadProperties(context, author).thenAccept(properties -> {
            String score = properties.getScore().equals("Unranked")
                ? "Unranked"
                : properties.getScore() + " / " + getUsersInGuild(context.getGuild());

            long zeroExperience = avaire.getLevelManager().getExperienceFromLevel(guildTransformer, 0) - 100;
            long experience = properties.getPlayer().getExperience() + zeroExperience;

            long level = avaire.getLevelManager().getLevelFromExperience(guildTransformer, experience);
            long current = avaire.getLevelManager().getExperienceFromLevel(guildTransformer, level);

            long nextLevelXp = avaire.getLevelManager().getExperienceFromLevel(guildTransformer, level + 1);
            double percentage = ((double) (experience - current) / (nextLevelXp - current)) * 100;

            String levelBar = "";
            for (int i = 1; i <= 40; i++) {
                levelBar += ((i * 2.5) < percentage) ? "\u2592" : "\u2591";
            }

            PlayerTransformer playerTransformer = PlayerController.fetchPlayer(avaire, context.getMessage(), author);
            if (playerTransformer != null) {
                Integer selectedBackgroundId = playerTransformer.getPurchases()
                    .getSelectedPurchasesForType(
                        RankBackgrounds.getDefaultBackground().getPurchaseType()
                    );

                if (selectedBackgroundId != null) {
                    sendBackgroundMessage(
                        context, author,
                        score, levelBar,
                        level, nextLevelXp,
                        experience, zeroExperience,
                        percentage,
                        properties,
                        selectedBackgroundId
                    );
                    return;
                }
            }

            sendEmbeddedMessage(
                context, author,
                score, levelBar,
                level, nextLevelXp,
                experience, zeroExperience,
                percentage,
                properties
            );
        });

        return true;
    }

    private void sendEmbeddedMessage(
        CommandMessage context,
        User author,
        String score,
        String levelBar,
        long level,
        long nextLevelXp,
        long experience,
        long zeroExperience,
        double percentage,
        DatabaseProperties properties
    ) {
        MessageFactory.makeEmbeddedMessage(context.getChannel(), Color.decode("#E91E63"))
            .setAuthor(author.getName(), "https://avairebot.com/leaderboard/" + context.getGuild().getId(), author.getEffectiveAvatarUrl())
            .setFooter("https://avairebot.com/leaderboard/" + context.getGuild().getId())
            .addField(context.i18n("fields.rank"), score, true)
            .addField(context.i18n("fields.level"), NumberUtil.formatNicely(level), true)
            .addField(context.i18n("fields.experience"), (experience - zeroExperience - 100 < 0 ? "0" : context.i18n("fields.total",
                NumberUtil.formatNicely(experience - zeroExperience - 100), NumberUtil.formatNicely(properties.getTotal())
            )), true)
            .addField(context.i18n("fields.experienceToNext"), context.i18n("fields.youNeedMoreXpToLevelUp",
                levelBar, NumberUtil.formatNicelyWithDecimals(percentage), '%', NumberUtil.formatNicely(nextLevelXp - experience)
            ), false)
            .requestedBy(context.getMember())
            .queue();
    }

    private void sendBackgroundMessage(
        CommandMessage context,
        User author,
        String score,
        String levelBar,
        long level,
        long nextLevelXp,
        long experience,
        long zeroExperience,
        double percentage,
        DatabaseProperties properties,
        int backgroundId
    ) {
        RankBackgrounds background = RankBackgrounds.fromId(backgroundId);
        if (background == null) {
            sendEmbeddedMessage(
                context, author,
                score, levelBar,
                level, nextLevelXp,
                experience, zeroExperience,
                percentage,
                properties
            );
            return;
        }

        long xpForCurrentLevel = avaire.getLevelManager().getExperienceFromLevel(
            context.getGuildTransformer(), level
        );

        RankBackgroundRender render = new RankBackgroundRender(author)
            .setBackground(background)
            .setCurrentXpInLevel(NumberUtil.formatNicely(experience - xpForCurrentLevel))
            .setTotalXpInLevel(NumberUtil.formatNicely(nextLevelXp - xpForCurrentLevel))
            .setGlobalExperience(NumberUtil.formatNicely(properties.getTotal()))
            .setServerExperience(NumberUtil.formatNicely(experience - zeroExperience - 100))
            .setLevel(NumberUtil.formatNicely(level))
            .setRank(properties.getScore().equalsIgnoreCase("Unranked") ? "???" : properties.getScore())
            .setPercentage(percentage);

        String attachmentName = I18n.format(
            "{0}-{1}-rank-bg.png",
            context.getGuild().getIdLong(),
            author.getId()
        );

        MessageBuilder message = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder()
            .setImage("attachment://" + attachmentName)
            .setColor(background.getBackgroundColors().getExperienceForegroundColor());
        message.setEmbed(embed.build());

        try {
            //noinspection ConstantConditions
            context.getMessageChannel().sendFile(
                new ByteArrayInputStream(render.renderToBytes()),
                attachmentName, message.build()
            ).queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CompletableFuture<DatabaseProperties> loadProperties(CommandMessage context, User author) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PlayerTransformer player = context.getAuthor().getIdLong() == author.getIdLong()
                    ? context.getPlayerTransformer() : PlayerController.fetchPlayer(avaire, context.getMessage(), author);

                DataRow data = avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                    .selectRaw("sum(`experience`) - (count(`user_id`) * 100) as `total`")
                    .where("user_id", author.getId())
                    .where("active", 1)
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
        Collection users = (Collection) CacheUtil.getUncheckedUnwrapped(cache, context.getGuild().getIdLong(), () ->
            avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                .select("user_id as id")
                .orderBy("experience", "desc")
                .where("guild_id", context.getGuild().getId())
                .where("active", 1)
                .get()
        );

        for (int i = 0; i < users.size(); i++) {
            if (Objects.equals(users.get(i).getString("id"), userId)) {
                return "" + (i + 1);
            }
        }

        return context.i18n("unranked");
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

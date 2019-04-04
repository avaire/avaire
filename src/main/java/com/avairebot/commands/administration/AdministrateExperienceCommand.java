/*
 * Copyright (c) 2019.
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

package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.utility.RankCommand;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.controllers.PlayerController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlayerTransformer;
import com.avairebot.level.LevelManager;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RandomUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AdministrateExperienceCommand extends Command {

    public static final Cache<String, String> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(60, TimeUnit.SECONDS)
        .build();

    private static final Logger log = LoggerFactory.getLogger(AdministrateExperienceCommand.class);

    public AdministrateExperienceCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Administrate Experience Command";
    }

    @Override
    public String getDescription() {
        return "This command can be used to give, take, or reset a users XP, on a per-server basis.\n**Note:** This does not affect the global leaderboard.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command reset <user>` - Resets the users XP.",
            "`:command add <user> <amount>` - Adds the given amount of XP to the user.",
            "`:command take <user> <amount>` - Takes the given amount of XP from the user.",
            "`:command server-reset` - Resets the XP for everyone on the entire server in one go."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command reset @Senither`",
            "`:command add @Senither 9999`",
            "`:command take @Senither 1337`",
            "`:command server-reset`"
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            LevelCommand.class,
            RankCommand.class,
            AddLevelRoleCommand.class,
            ListLevelRolesCommand.class,
            RemoveLevelRoleCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("experience", "xp");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:guild,1,5"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(
            CommandGroups.LEVEL_AND_EXPERIENCE
        );
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null || !guildTransformer.isLevels()) {
            return sendErrorMessage(context, "errors.requireLevelFeatureToBeEnabled",
                CommandHandler.getCommand(LevelCommand.class)
                    .getCommand().generateCommandTrigger(context.getMessage())
            );
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "action");
        }

        Action action = Action.fromTrigger(args[0].trim());
        if (action == null) {
            return sendErrorMessage(context, "errors.invalidProperty", "action", "action");
        }

        if (action.equals(Action.SERVER_RESET)) {
            return handleServerReset(context, Arrays.copyOfRange(args, 1, args.length));
        }

        if (args.length == 1) {
            return sendErrorMessage(context, "errors.missingArgument", "user");
        }

        User user = MentionableUtil.getUser(context, args, 1);
        if (user == null) {
            return sendErrorMessage(context, "errors.invalidProperty", "user", "user");
        }

        if (user.isBot()) {
            return sendErrorMessage(context, context.i18n("cantUseForBots"));
        }

        long amount = -1;
        if (action.isAmount()) {
            if (args.length == 2) {
                return sendErrorMessage(context, "errors.missingArgument", "amount");
            }

            try {
                amount = Long.parseLong(args[2]);
            } catch (NumberFormatException e) {
                if (NumberUtil.isNumeric(args[2])) {
                    BigInteger integer = new BigInteger(args[2]);
                    if (integer.compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) >= 0) {
                        amount = Long.MAX_VALUE;
                    }
                }

                if (amount < 0) {
                    return sendErrorMessage(context, "errors.invalidProperty", "amount", "number");
                }
            }

            if (amount < 1) {
                return sendErrorMessage(context, context.i18n("invalidAmountGiven"),
                    NumberUtil.formatNicely(LevelManager.getHardCap())
                );
            }
        }

        PlayerTransformer player = PlayerController.fetchPlayer(avaire, context.getMessage(), user);
        if (player == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "player XP");
        }

        switch (action) {
            case ADD:
                return handleAdd(context, player, user, amount);

            case TAKE:
                return handleTake(context, player, user, amount);

            case RESET:
                return handleReset(context, player, user);
        }

        return sendErrorMessage(context, "If you're seeing this message, something went horribly wrong.");
    }

    private boolean handleAdd(CommandMessage context, PlayerTransformer player, User user, long amount) {
        long currentXP = player.getExperience();

        player.incrementExperienceBy(amount);
        if (player.getExperience() < 100) {
            player.setExperience(LevelManager.getHardCap());
        }

        if (!updatePlayerRecord(player)) {
            player.setExperience(currentXP);
            return sendErrorMessage(context, context.i18n("failedToSaveChanges"));
        }

        context.makeSuccess(context.i18n("success.add"))
            .set("amount", NumberUtil.formatNicely(amount))
            .set("newAmount", NumberUtil.formatNicely(player.getExperience() - 100))
            .set("target", user.getAsMention())
            .queue();

        return true;
    }

    private boolean handleTake(CommandMessage context, PlayerTransformer player, User user, long amount) {
        long currentXP = player.getExperience();

        player.incrementExperienceBy(amount * -1);
        if (player.getExperience() < 100) {
            player.setExperience(100);
        }

        if (!updatePlayerRecord(player)) {
            player.setExperience(currentXP);
            return sendErrorMessage(context, context.i18n("failedToSaveChanges"));
        }

        context.makeSuccess(context.i18n("success.take"))
            .set("amount", NumberUtil.formatNicely(amount))
            .set("newAmount", NumberUtil.formatNicely(player.getExperience() - 100))
            .set("target", user.getAsMention())
            .queue();

        return true;
    }

    private boolean handleReset(CommandMessage context, PlayerTransformer player, User user) {
        long currentXP = player.getExperience();

        player.setExperience(100);

        if (!updatePlayerRecord(player)) {
            player.setExperience(currentXP);
            return sendErrorMessage(context, context.i18n("failedToSaveChanges"));
        }

        context.makeSuccess(context.i18n("success.reset"))
            .set("target", user.getAsMention())
            .queue();

        return true;
    }

    private boolean handleServerReset(CommandMessage context, String[] args) {
        if (!context.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            return sendErrorMessage(context, context.i18n("mustBeAnAdmin"));
        }

        String cacheKey = context.getGuild().getId() + ":" + context.getAuthor().getId();
        String token = cache.getIfPresent(cacheKey);

        if (token == null) {
            token = RandomUtil.generateString(RandomUtil.getInteger(3) + 4);

            context.makeWarning(context.i18n("aboutToResetEverything"))
                .setTitle(context.i18n("warning"))
                .set("command", generateCommandTrigger(context.getMessage()))
                .set("token", token)
                .queue();

            cache.put(cacheKey, token);

            return false;
        }

        if (args.length == 0 || !args[0].equals(token)) {
            return sendErrorMessage(context, context.i18n("invalidSecurityToken"));
        }

        try {
            avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                .where("guild_id", context.getGuild().getId())
                .update(statement -> {
                    statement.set("experience", 100);
                    statement.set("active", 0);
                });

            PlayerController.forgetCacheForGuild(context.getGuild().getIdLong());

            context.makeSuccess(context.i18n("success.everything"))
                .queue();
        } catch (SQLException e) {
            log.error("Failed to reset server XP for {}, error: {}", context.getGuild().getId(), e.getMessage(), e);

            return sendErrorMessage(context, context.i18n("failedToSaveChanges"));
        }

        return true;
    }

    private boolean updatePlayerRecord(PlayerTransformer player) {
        try {
            avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                .where("user_id", player.getUserId())
                .where("guild_id", player.getGuildId())
                .update(statement -> statement.set("experience", player.getExperience()));
        } catch (SQLException e) {
            log.error("Failed to update player transformer for {} in {} server, error: {}",
                player.getUserId(), player.getGuildId(), e.getMessage(), e
            );
            return false;
        }

        return true;
    }

    enum Action {

        ADD("add", "give"),
        TAKE("take", "remove"),
        RESET(false, "reset"),
        SERVER_RESET(false, "server-reset");

        private boolean amount;
        private List<String> triggers;

        Action(boolean amount, String... triggers) {
            this.amount = amount;
            this.triggers = Arrays.asList(triggers);
        }

        Action(String... triggers) {
            this(true, triggers);
        }

        @Nullable
        public static Action fromTrigger(String trigger) {
            for (Action action : values()) {
                if (action.getTriggers().contains(trigger.toLowerCase())) {
                    return action;
                }
            }
            return null;
        }

        public boolean isAmount() {
            return amount;
        }

        public List<String> getTriggers() {
            return triggers;
        }
    }
}

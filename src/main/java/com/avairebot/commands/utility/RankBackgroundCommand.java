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

package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.controllers.PurchaseController;
import com.avairebot.database.transformers.PlayerTransformer;
import com.avairebot.imagegen.RankBackgrounds;
import com.avairebot.imagegen.renders.RankBackgroundRender;
import com.avairebot.shared.DiscordConstants;
import com.avairebot.utilities.ComparatorUtil;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RandomUtil;
import com.avairebot.vote.VoteCacheEntity;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RankBackgroundCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(RankBackgroundCommand.class);

    public RankBackgroundCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Rank Background Command";
    }

    @Override
    public String getDescription(@Nullable CommandContext context) {
        String prefix = context != null && context.isGuildMessage()
            ? generateCommandPrefix(context.getMessage())
            : DiscordConstants.DEFAULT_COMMAND_PREFIX;

        return String.format(String.join("\n",
            "Rank backgrounds are used for the `%srank` command, when a user has a rank",
            "background selected, their rank and experience will be displayed using a generated",
            "image instead, the background image can be changed at any time.",
            "You can buy backgrounds using [vote points](https://discordbots.org/bot/avaire), use `%svote` for more info."
        ), prefix, prefix);
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command list` - Lists all the backgrounds available.",
            "`:command test <name>` - Displays an example of how the background will look like if you buy it.",
            "`:command buy <name>` - Buys the background with vote points.",
            "`:command use <name>` - Selects the background so it is used in the future for rank commands.",
            "`:command off` - Disables the rank background, returns to using embedded rank messages."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command list`",
            "`:command test discord dark theme`",
            "`:command buy discord dark theme`",
            "`:command use discord dark theme`",
            "`:command off`"
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(
            RankCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("backgrounds", "rankbg", "levelbg", "bg");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,1,5");
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.LEVEL_AND_EXPERIENCE);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "option");
        }

        switch (args[0].toLowerCase().trim()) {
            case "l":
            case "list":
                return handleList(context, prepareArguments(args));

            case "s":
            case "show":
            case "test":
            case "example":
                return handleShow(context, prepareArguments(args));

            case "b":
            case "buy":
            case "purchases":
                return handlePurchases(context, prepareArguments(args));

            case "u":
            case "use":
            case "select":
                return handleSelect(context, prepareArguments(args));

            default:
                if (ComparatorUtil.isFuzzyFalse(args[0])) {
                    return handleDisable(context);
                }
                return sendErrorMessage(context, "errors.invalidProperty", "option", "option");
        }
    }

    private boolean handleList(CommandMessage context, String[] args) {
        PlayerTransformer player = context.getPlayerTransformerWithForce(avaire);
        if (player == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "player transformer");
        }

        VoteCacheEntity voteEntity = avaire.getVoteManager().getVoteEntity(context.getAuthor());
        int votePoints = voteEntity == null ? 0 : voteEntity.getVotePoints();

        SimplePaginator<Integer> paginator = new SimplePaginator<>(
            RankBackgrounds.getNameToCost(), 5, 1
        );

        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        List<String> message = new ArrayList<>();
        message.add(context.i18n("canBeUnlocked"));
        message.add("-------------------------------");

        String purchaseType = RankBackgrounds.getDefaultBackground().getPurchaseType();
        paginator.forEach((index, name, cost) -> {
            //noinspection ConstantConditions
            boolean alreadyOwns = player.hasPurchases() && player.getPurchases().hasPurchase(
                purchaseType, RankBackgrounds.fromName((String) name).getId()
            );

            message.add(context.i18n(alreadyOwns ? "buyNotes.alreadyOwns" : "buyNotes.doesntOwns",
                name, cost
            ));
        });

        message.add("-------------------------------");
        message.add(paginator.generateFooter(context.getGuild(), generateCommandTrigger(context.getMessage()) + " list"));

        context.makeInfo(String.join("\n", message))
            .setTitle(context.i18n("listTitle", paginator.getTotal()))
            .setFooter(context.i18n("youHaveVotePoints", votePoints))
            .queue();

        return false;
    }

    private boolean handleShow(CommandMessage context, String[] args) {
        RankBackgrounds background = RankBackgrounds.fromName(String.join(" ", args));
        if (background == null) {
            return sendErrorMessage(context, "errors.invalidProperty", "background name", "background");
        }

        long zeroExperience = avaire.getLevelManager().getExperienceFromLevel(0) - 100;
        long experience = 74187 + zeroExperience + RandomUtil.getInteger(1433);

        long level = avaire.getLevelManager().getLevelFromExperience(experience);
        long current = avaire.getLevelManager().getExperienceFromLevel(level);

        long nextLevelXp = avaire.getLevelManager().getExperienceFromLevel(level + 1);
        double percentage = ((double) (experience - current) / (nextLevelXp - current)) * 100;

        RankBackgroundRender render = new RankBackgroundRender(context.getAuthor())
            .setBackground(background)
            .setCurrentXpInLevel(NumberUtil.formatNicely(nextLevelXp - experience))
            .setTotalXpInLevel(NumberUtil.formatNicely(nextLevelXp - current))
            .setGlobalExperience(NumberUtil.formatNicely(99999))
            .setServerExperience(NumberUtil.formatNicely(experience - zeroExperience - 100))
            .setLevel(NumberUtil.formatNicely(level))
            .setRank("1")
            .setPercentage(percentage);

        MessageBuilder message = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(context.i18n("exampleTitle", background.getName()))
            .setFooter(context.i18n("exampleFooter", background.getName(), background.getCost()), null)
            .setImage("attachment://rank-background.png")
            .setColor(background.getBackgroundColors().getExperienceForegroundColor());
        message.setEmbed(embed.build());

        try {
            //noinspection ConstantConditions
            context.getMessageChannel().sendFile(
                new ByteArrayInputStream(render.renderToBytes()),
                "rank-background.png", message.build()
            ).queue();
        } catch (IOException e) {
            log.error("Failed to render background image: {}", e.getMessage(), e);
            return sendErrorMessage(context, context.i18n("failedToSendExampleMessage",
                background.getName(), e.getMessage()
            ));
        }

        return true;
    }

    private boolean handlePurchases(CommandMessage context, String[] args) {
        RankBackgrounds background = RankBackgrounds.fromName(String.join(" ", args));
        if (background == null) {
            return sendErrorMessage(context, "errors.invalidProperty", "background name", "background");
        }

        PlayerTransformer player = context.getPlayerTransformerWithForce(avaire);
        if (player == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "player transformer");
        }

        if (player.getPurchases().hasPurchase(background.getPurchaseType(), background.getId())) {
            context.makeWarning(context.i18n("alreadyOwnsBackground"))
                .set("name", background.getName())
                .queue();
            return false;
        }

        VoteCacheEntity voteEntity = avaire.getVoteManager().getVoteEntity(context.getAuthor());
        int votePoints = voteEntity == null ? 0 : voteEntity.getVotePoints();

        if (background.getCost() > votePoints) {
            return sendErrorMessage(context, context.i18n("doesntHaveEnoughPoints",
                NumberUtil.formatNicely(background.getCost()), NumberUtil.formatNicely(votePoints)
            ));
        }

        try {
            avaire.getDatabase().newQueryBuilder(Constants.PURCHASES_TABLE_NAME)
                .insert(statement -> {
                    statement.set("user_id", context.getAuthor().getIdLong());
                    statement.set("type", background.getPurchaseType());
                    statement.set("type_id", background.getId());
                });

            avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                .where("user_id", context.getAuthor().getIdLong())
                .update(statement -> {
                    statement.setRaw("points", "`points` - " + background.getCost());
                });

            if (voteEntity != null) {
                voteEntity.setVotePoints(votePoints - background.getCost());
            }

            PurchaseController.forgetCache(context.getAuthor().getIdLong());
        } catch (SQLException e) {
            log.error("Something went wrong while a use was trying to buy a background: {}", e.getMessage(), e);

            return sendErrorMessage(context, context.i18n("failedToBuyTheBackground", e.getMessage()));
        }

        context.makeSuccess(context.i18n("boughtBackgroundSuccessfully"))
            .set("name", background.getName())
            .set("command", generateCommandTrigger(context.getMessage()) + " use " + background.getName())
            .queue();

        return true;
    }

    private boolean handleSelect(CommandMessage context, String[] args) {
        RankBackgrounds background = RankBackgrounds.fromName(String.join(" ", args));
        if (background == null) {
            return sendErrorMessage(context, "errors.invalidProperty", "background name", "background");
        }

        PlayerTransformer player = context.getPlayerTransformerWithForce(avaire);
        if (player == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "player transformer");
        }

        if (!player.getPurchases().hasPurchase(background.getPurchaseType(), background.getId())) {
            context.makeWarning(context.i18n("youDontOwnThisBackground"))
                .set("name", background.getName())
                .set("cost", background.getCost())
                .queue();

            return false;
        }

        try {
            avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                .where("user_id", context.getAuthor().getIdLong())
                .update(statement -> statement.set("selected_bg", background.getId()));

            PurchaseController.forgetCache(context.getAuthor().getIdLong());

            context.makeSuccess(context.i18n("successfullyChangedBackground"))
                .set("name", background.getName())
                .queue();
        } catch (SQLException e) {
            log.error("Failed to set background for user {}, error: {}", context.getAuthor().getId(), e.getMessage(), e);

            return sendErrorMessage(context, context.i18n("failedToSetBackground"));
        }

        return true;
    }

    private boolean handleDisable(CommandMessage context) {
        PlayerTransformer player = context.getPlayerTransformerWithForce(avaire);
        if (player == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "player transformer");
        }

        try {
            if (player.getPurchases().getSelectedPurchasesForType(RankBackgrounds.getDefaultBackground().getPurchaseType()) != null) {
                avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                    .where("user_id", context.getAuthor().getIdLong())
                    .update(statement -> statement.set("selected_bg", null));

                PurchaseController.forgetCache(context.getAuthor().getIdLong());
            }

            context.makeSuccess(context.i18n("disabledBackgrounds"))
                .queue();
        } catch (SQLException e) {
            log.error("Failed to reset background for user {}, error: {}", context.getAuthor().getId(), e.getMessage(), e);

            return sendErrorMessage(context, context.i18n("failedToSetBackground"));
        }

        return true;
    }

    private String[] prepareArguments(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }
}
